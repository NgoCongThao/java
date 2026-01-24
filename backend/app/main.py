from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import chat

app = FastAPI()

# Cấu hình CORS (Cho phép Frontend gọi vào)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # Trong thực tế nên để http://localhost:3000
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(chat.router, prefix="/api")

@app.get("/")
def read_root():
    return {"status": "Chatbot Backend is running"}