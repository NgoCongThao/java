from langchain_ollama import OllamaLLM, OllamaEmbeddings
from langchain_chroma import Chroma
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser
from app.core.config import settings

# 1. Khởi tạo Model & DB
embedding = OllamaEmbeddings(model=settings.EMBEDDING_MODEL)
llm = OllamaLLM(model=settings.OLLAMA_MODEL)

def get_vector_db():
    return Chroma(
        persist_directory=settings.VECTOR_DB_PATH,
        embedding_function=embedding
    )

# 2. Định nghĩa Prompt (Kịch bản)
template = """
Bạn là trợ lý AI hữu ích. Hãy trả lời câu hỏi dựa trên ngữ cảnh được cung cấp bên dưới.
Nếu không tìm thấy thông tin trong ngữ cảnh, hãy nói "Tôi không tìm thấy thông tin này trong dữ liệu hệ thống".

<context>
{context}
</context>

Câu hỏi: {question}
"""
prompt = ChatPromptTemplate.from_template(template)

# 3. Hàm format documents (Ghép các đoạn văn bản tìm được thành 1 chuỗi)
def format_docs(docs):
    return "\n\n".join(doc.page_content for doc in docs)

def get_answer(query: str):
    vector_db = get_vector_db()
    
    # Tạo Retriever (Bộ tìm kiếm)
    retriever = vector_db.as_retriever(search_kwargs={"k": 3})
    
    # --- PHẦN QUAN TRỌNG: LCEL CHAIN (Thay thế cho create_retrieval_chain cũ) ---
    # Luồng dữ liệu:
    # 1. Lấy context từ retriever & Lấy question từ input
    # 2. Đưa vào prompt
    # 3. Đưa vào LLM
    # 4. Parse ra string
    rag_chain = (
        {"context": retriever | format_docs, "question": RunnablePassthrough()}
        | prompt
        | llm
        | StrOutputParser()
    )
    
    # Chạy chuỗi xử lý
    try:
        answer = rag_chain.invoke(query)
        
        # Lấy nguồn thủ công (vì LCEL trả về string, ta cần query lại retriever để lấy source)
        # Bước này để hiển thị nguồn cho đẹp
        source_docs = retriever.invoke(query)
        sources = list(set([doc.metadata.get('source', 'Unknown').split("\\")[-1].split("/")[-1] for doc in source_docs]))
        
        return {
            "answer": answer,
            "sources": sources
        }
    except Exception as e:
        print(f"Lỗi RAG: {e}")
        return {"answer": "Đã xảy ra lỗi khi xử lý câu hỏi.", "sources": []}