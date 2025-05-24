# ollama
Spring AI implemtation with ollama and chroma


# SpringBoardAI

SpringBoardAI is a Spring Boot 3.4 backend demonstrating usage of Spring AI (version 1.0.0) with a local Ollama (Mistral and Llama2) LLM and Chroma vector DB. It provides REST endpoints for question-answering, conversational chat, document upload, and RAG-based chat.

## Features

- **/qa** (GET) – Answer a single user question with Ollama (Mistral). 
- **/chat** (POST) – Conversational chat using a user prompt and chat history, preserving context.
- **/upload** (POST) – Upload a PDF, DOCX, or TXT file; text is parsed and ingested into Chroma DB for similarity search:contentReference[oaicite:3]{index=3}.
- **/rag-chat** (POST) – Context-aware chat: retrieves relevant uploaded documents from Chroma and asks the LLM (RAG technique):contentReference[oaicite:4]{index=4}

## Technology

- **Spring Boot & Spring AI** – Simplifies integration with Ollama and vector stores.
- **Ollama (Mistral)** – Local LLM for chat and embeddings (chat model: *mistral*, embedding model: *mistral*).
- **ChromaDB** – Local vector database (Docker) for storing embeddings and performing similarity search:contentReference[oaicite:6]{index=6}.
- **Apache PDFBox / POI** – Parsing PDF and DOCX files for ingestion.
- **REST API** – All endpoints use HTTP (no WebSockets or streaming).

## Setup

1. **Chroma DB:** Ensure Docker is running, then start Chroma
