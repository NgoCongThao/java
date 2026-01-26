import os
from dotenv import load_dotenv
from langchain_community.document_loaders import DirectoryLoader, TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter, Language
from langchain_ollama import OllamaEmbeddings
from langchain_chroma import Chroma

# Load biến môi trường trực tiếp ở đây để chạy độc lập
load_dotenv()

ROOT_DIR = os.getenv("PROJECT_ROOT_PATH", "../")
DB_DIR = os.getenv("VECTOR_DB_PATH", "./vector_db")
EMBED_MODEL = os.getenv("EMBEDDING_MODEL", "nomic-embed-text")

def ingest_data():
    print(f"🚀 Bắt đầu quét dữ liệu từ: {os.path.abspath(ROOT_DIR)}")
    
    # 1. Quét tìm file .js
    loader = DirectoryLoader(
        path=ROOT_DIR,
        glob="**/data*.js", # Chỉ tìm file có tên bắt đầu bằng data...js
        loader_cls=TextLoader,
        loader_kwargs={"encoding": "utf-8"}, # Bắt buộc utf-8 để không lỗi font
        # Loại bỏ các folder rác
        exclude=["**/node_modules/**", "**/.git/**", "**/dist/**", "**/backend/**", "**/frontend/**"],
        show_progress=True
    )
    
    try:
        docs = loader.load()
        if not docs:
            print("⚠️ Không tìm thấy file 'data*.js' nào. Hãy kiểm tra lại folder.")
            return
        print(f"✅ Tìm thấy {len(docs)} file.")
    except Exception as e:
        print(f"❌ Lỗi khi đọc file: {e}")
        return

    # 2. Chia nhỏ văn bản (Chunking)
    print("✂️ Đang chia nhỏ dữ liệu...")
    text_splitter = RecursiveCharacterTextSplitter.from_language(
        language=Language.JS,
        chunk_size=1000,
        chunk_overlap=100
    )
    chunks = text_splitter.split_documents(docs)
    print(f"-> Tạo được {len(chunks)} đoạn dữ liệu (chunks).")

    # 3. Mã hóa và lưu vào DB
    print(f"💾 Đang lưu vào ChromaDB ({DB_DIR})...")
    embedding = OllamaEmbeddings(model=EMBED_MODEL)
    
    # Xóa DB cũ nếu muốn làm mới hoàn toàn (Optional)
    # import shutil
    # if os.path.exists(DB_DIR): shutil.rmtree(DB_DIR)

    Chroma.from_documents(
        documents=chunks,
        embedding=embedding,
        persist_directory=DB_DIR
    )
    print("🎉 HOÀN TẤT! Dữ liệu đã sẵn sàng.")

if __name__ == "__main__":
    ingest_data()