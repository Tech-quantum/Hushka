interface AiProvider {
    suspend fun generate(request: AiRequest): AiResult
}
