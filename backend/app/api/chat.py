from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.services.rag_engine import get_answer

router = APIRouter()

class ChatRequest(BaseModel):
    question: str

@router.post("/chat")
async def chat(request: ChatRequest):
    try:
        result = get_answer(request.question)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))