package com.linagora.tmail.mailet;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.james.core.MailAddress;
import org.apache.mailet.MailetConfig;
import org.apache.mailet.MailetException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;


public class AIBotConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AIBotConfig.class);

    public static String API_KEY_PARAMETER_NAME = "apiKey";
    public static String GPT_ADDRESS_PARAMETER_NAME = "gptAddress";
    public static String MODEL_PARAMETER_NAME = "model";
    public static String BASE_URL_PARAMETER_NAME = "baseURL";

    public static final LlmModel DEFAULT_LLM_MODEL =
            new LlmModel(Llm.OPEN_AI, "gpt-4o-mini");

    private final String apiKey;
    private final URL baseURL;
    private final MailAddress gptAddress;
    private final LlmModel llmModel;

    public AIBotConfig(String apiKey, MailAddress gptAddress, LlmModel llmModel, @Nullable URL baseURL) {
        Objects.requireNonNull(apiKey);
        Objects.requireNonNull(gptAddress);
        Objects.requireNonNull(llmModel);

        this.apiKey = apiKey;
        this.baseURL = baseURL;
        this.gptAddress = gptAddress;
        this.llmModel = llmModel;
    }

    public static AIBotConfig fromMailetConfig(MailetConfig mailetConfig) throws MailetException {
        String apiKeyParam = mailetConfig.getInitParameter(API_KEY_PARAMETER_NAME);
        String gptAddressParam = mailetConfig.getInitParameter(GPT_ADDRESS_PARAMETER_NAME);
        String llmModelParam = mailetConfig.getInitParameter(MODEL_PARAMETER_NAME);
        String baseUrlParam = mailetConfig.getInitParameter(BASE_URL_PARAMETER_NAME);

        if (Strings.isNullOrEmpty(apiKeyParam)) {
            throw new MailetException("No value for " + API_KEY_PARAMETER_NAME + " parameter was provided.");
        }

        if (Strings.isNullOrEmpty(gptAddressParam)) {
            throw new MailetException("No value for " + GPT_ADDRESS_PARAMETER_NAME + " parameter was provided.");
        }

        Optional<URL> baseURLOpt = toOptionalIfNotEmpty(baseUrlParam)
                .flatMap(AIBotConfig::baseURLStringToURL);

        LlmModel llmModel = !Strings.isNullOrEmpty(llmModelParam)
                            ? parseLlmModelParamOrThrow(llmModelParam)
                            : DEFAULT_LLM_MODEL;

        try {
            return new AIBotConfig(apiKeyParam, new MailAddress(gptAddressParam), llmModel, baseURLOpt.orElse(null));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static @NotNull Optional<URL> baseURLStringToURL(String baseUrlString) {
        try {
            return Optional.of(URI.create(baseUrlString).toURL());
        } catch (MalformedURLException e) {
            LOGGER.warn("Invalid LLM API base URL", e);
        }

        return Optional.empty();
    }

    private static LlmModel parseLlmModelParamOrThrow(String llmModelParam) throws MailetException {
        String[] tokens = llmModelParam.split(",");

        if (tokens.length != 2) {
            throw new MailetException("""
                    Invalid LLM model parameter. Please add the LLM along with the specific model to use.
                    Example: openai,gpt-4-mini
                    Provided value: %s""".formatted(llmModelParam));
        }

        String llm = tokens[0];
        String model = tokens[1];

        Optional<Llm> llmOpt = Llm.fromName(llm);
        if (llmOpt.isEmpty()) {
            String supportedLLms = Llm.getSupportedLlmNames().stream()
                    .collect(Collectors.joining(", ", "[", "]"));

            throw new MailetException("Unsupported LLM provided '%s'. Supported LLMs: %s".formatted(llm, supportedLLms));
        }

        return new LlmModel(llmOpt.get(), model);
    }

    public String getApiKey() {
        return apiKey;
    }

    public MailAddress getGptAddress() {
        return gptAddress;
    }

    public LlmModel getLlmModel() {
        return llmModel;
    }

    public Optional<URL> getBaseURL() {
        if (baseURL == null) {
            return Optional.empty();
        } else {
            return Optional.of(baseURL);
        }
    }

    private static Optional<String> toOptionalIfNotEmpty(String s) {
        if (Strings.isNullOrEmpty(s)) {
            return Optional.empty();
        } else {
            return Optional.of(s);
        }
    }
}
