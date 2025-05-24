package com.example.demo.dto;

public class RAGRequest {

    private String prompt;

    public RAGRequest() {}
    public RAGRequest(String prompt) {
        this.prompt = prompt;
    }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
	@Override
	public String toString() {
		return "RAGRequest [prompt=" + prompt + "]";
	}
    

}
