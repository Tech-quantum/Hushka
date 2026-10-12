interface AiEngine {
    suspend fun analyzePrompt(text: String): PromptAiAnalysis
    suspend fun improvePrompt(text: String): String
    suspend fun generate(request: AiRequest): AiResult
}
