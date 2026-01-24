import os
from dotenv import load_dotenv
from langchain_community.document_loaders import DirectoryLoader, TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter, Language
from langchain_ollama import OllamaEmbeddings
from langchain_chroma import Chroma

# Load biến môi trường
load_dotenv()

ROOT_DIR = os.getenv("PROJECT_ROOT_PATH")
DB_DIR = os.getenv("VECTOR_DB_PATH")
EMBED_MODEL = os.getenv("EMBEDDING_MODEL")

def scan_and_ingest():
    print(f"🔄 Đang quét file 'data*.js' từ: {os.path.abspath(ROOT_DIR)}")

    # 1. Quét file (Loại bỏ folder rác)
    loader = DirectoryLoader(
        path=ROOT_DIR,
        glob="**/data*.js", 
        loader_cls=TextLoader,
        exclude=["**/node_modules/**", "**/.git/**", "**/backend/**", "**/frontend/**"],
        show_progress=True
    )

    try:
        docs = loader.load()
        if not docs:
            print("⚠️ Không tìm thấy file nào! Kiểm tra lại đường dẫn.")
            return
        print(f"✅ Tìm thấy {len(docs)} file.")
    except Exception as e:
        print(f"❌ Lỗi đọc file: {e}")
        return

    # 2. Chia nhỏ code JS
    text_splitter = RecursiveCharacterTextSplitter.from_language(
        language=Language.JS, chunk_size=1000, chunk_overlap=100
    )
    chunks = text_splitter.split_documents(docs)
    
    # 3. Embed & Lưu DB
    print("💾 Đang lưu vào Vector DB...")
    embedding = OllamaEmbeddings(model=EMBED_MODEL)
    Chroma.from_documents(documents=chunks, embedding=embedding, persist_directory=DB_DIR)
    print("🎉 Xong! Dữ liệu đã sẵn sàng.")

if __name__ == "__main__":
    scan_and_ingest()