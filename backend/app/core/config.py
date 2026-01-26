import os
from dotenv import load_dotenv

load_dotenv()

class Settings:
    PROJECT_NAME = "Chatbot RAG System"
    # Lấy biến môi trường, nếu không có thì dùng giá trị mặc định
    PROJECT_ROOT = os.getenv("PROJECT_ROOT_PATH", "../")
    VECTOR_DB_PATH = os.getenv("VECTOR_DB_PATH", "./vector_db")
    OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "phi3")
    EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "nomic-embed-text")

settings = Settings()