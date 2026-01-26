from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.services.rag_engine import get_answer

router = APIRouter()

class ChatRequest(BaseModel):
    question: str

@router.post("/chat")
async def chat_endpoint(request: ChatRequest):
    try:
        # Gọi hàm xử lý AI
        result = get_answer(request.question)
        return result
    except Exception as e:
        print(f"Error: {e}")
        raise HTTPException(status_code=500, detail=str(e))