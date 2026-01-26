from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import chat

app = FastAPI()

# Cấu hình CORS (Cho phép Frontend gọi vào)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # Cho phép tất cả các nguồn (dùng cho dev)
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Đăng ký router
app.include_router(chat.router, prefix="/api")

@app.get("/")
def root():
    return {"message": "Chatbot Backend is running!"}