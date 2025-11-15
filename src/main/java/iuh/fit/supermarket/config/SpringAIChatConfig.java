package iuh.fit.supermarket.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Spring AI ChatClient
 * Sử dụng ChatModel được auto-configured bởi Spring AI starter
 */
@Configuration
public class SpringAIChatConfig {

    /**
     * Tạo ChatClient bean từ ChatModel
     * ChatModel được auto-configured từ spring.ai.openai trong application.yml
     *
     * @param chatModel Spring AI ChatModel (Gemini API compatible)
     * @return ChatClient instance với logging enabled
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor()) // Log request/response để debug
                .build();
    }
}
