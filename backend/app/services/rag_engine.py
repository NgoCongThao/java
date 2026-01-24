from langchain_ollama import OllamaLLM, OllamaEmbeddings
from langchain_chroma import Chroma
from langchain.chains import RetrievalQA
from langchain.prompts import PromptTemplate
from app.core.config import settings

# Khởi tạo 1 lần để dùng lại
embedding = OllamaEmbeddings(model=settings.EMBEDDING_MODEL)
vector_db = Chroma(persist_directory=settings.VECTOR_DB_PATH, embedding_function=embedding)
llm = OllamaLLM(model=settings.OLLAMA_MODEL)

# Prompt template (Kịch bản trả lời)
template = """
Bạn là trợ lý AI hữu ích. Dựa vào đoạn code/dữ liệu sau để trả lời câu hỏi.
Nếu không biết, hãy nói "Tôi không tìm thấy thông tin".
Context: {context}
Câu hỏi: {question}
Trả lời:
"""
PROMPT = PromptTemplate(template=template, input_variables=["context", "question"])

def get_answer(query: str):
    qa_chain = RetrievalQA.from_chain_type(
        llm=llm,
        chain_type="stuff",
        retriever=vector_db.as_retriever(search_kwargs={"k": 3}),
        return_source_documents=True,
        chain_type_kwargs={"prompt": PROMPT}
    )
    
    response = qa_chain.invoke({"query": query})
    
    # Lấy nguồn file để tham khảo
    sources = list(set([doc.metadata.get('source', 'Unknown') for doc in response['source_documents']]))
    
    return {
        "answer": response['result'],
        "sources": sources
    }