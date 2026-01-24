import os
from dotenv import load_dotenv

load_dotenv()

class Settings:
    PROJECT_NAME = "RAG Chatbot"
    OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "phi3")
    EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "nomic-embed-text")
    VECTOR_DB_PATH = os.getenv("VECTOR_DB_PATH", "./vector_db")

settings = Settings()