package software.amazon.awssdk.services.query.endpoints.internal;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import software.amazon.awssdk.annotations.Generated;
import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.awscore.AwsExecutionAttribute;
import software.amazon.awssdk.awscore.endpoints.AwsEndpointAttribute;
import software.amazon.awssdk.awscore.endpoints.authscheme.EndpointAuthScheme;
import software.amazon.awssdk.core.SdkRequest;
import software.amazon.awssdk.core.SelectedAuthScheme;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.core.interceptor.SdkExecutionAttribute;
import software.amazon.awssdk.core.interceptor.SdkInternalExecutionAttribute;
import software.amazon.awssdk.endpoints.Endpoint;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.services.query.endpoints.QueryClientContextParams;
import software.amazon.awssdk.services.query.endpoints.QueryEndpointParams;
import software.amazon.awssdk.services.query.endpoints.QueryEndpointProvider;
import software.amazon.awssdk.services.query.model.OperationWithContextParamRequest;
import software.amazon.awssdk.utils.AttributeMap;

@Generated("software.amazon.awssdk:codegen")
@SdkInternalApi
public final class QueryResolveEndpointInterceptor implements ExecutionInterceptor {
    @Override
    public SdkRequest modifyRequest(Context.ModifyRequest context, ExecutionAttributes executionAttributes) {
        SdkRequest result = context.request();
        if (AwsEndpointProviderUtils.endpointIsDiscovered(executionAttributes)) {
            return result;
        }
        QueryEndpointProvider provider = (QueryEndpointProvider) executionAttributes
            .getAttribute(SdkInternalExecutionAttribute.ENDPOINT_PROVIDER);
        try {
            Endpoint endpoint = provider.resolveEndpoint(ruleParams(result, executionAttributes)).join();
            if (!AwsEndpointProviderUtils.disableHostPrefixInjection(executionAttributes)) {
                Optional<String> hostPrefix = hostPrefix(executionAttributes.getAttribute(SdkExecutionAttribute.OPERATION_NAME),
                                                         result);
                if (hostPrefix.isPresent()) {
                    endpoint = AwsEndpointProviderUtils.addHostPrefix(endpoint, hostPrefix.get());
                }
            }
            List<EndpointAuthScheme> authSchemes = endpoint.attribute(AwsEndpointAttribute.AUTH_SCHEMES);
            SelectedAuthScheme<?> selectedAuthScheme = executionAttributes
                .getAttribute(SdkInternalExecutionAttribute.SELECTED_AUTH_SCHEME);
            if (authSchemes != null && selectedAuthScheme != null) {
                selectedAuthScheme = AwsEndpointProviderUtils.mergeIntoResolvedAuthScheme(authSchemes, selectedAuthScheme);
                executionAttributes.putAttribute(SdkInternalExecutionAttribute.SELECTED_AUTH_SCHEME, selectedAuthScheme);
            }
            if (selectedAuthScheme != null) {
                AuthSchemeUtils.setSigningParams(executionAttributes, selectedAuthScheme.authSchemeOption());
            }
            executionAttributes.putAttribute(SdkInternalExecutionAttribute.RESOLVED_ENDPOINT, endpoint);
            return result;
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SdkClientException) {
                throw (SdkClientException) cause;
            } else {
                throw SdkClientException.create("Endpoint resolution failed", cause);
            }
        }
    }

    @Override
    public SdkHttpRequest modifyHttpRequest(Context.ModifyHttpRequest context, ExecutionAttributes executionAttributes) {
        Endpoint resolvedEndpoint = executionAttributes.getAttribute(SdkInternalExecutionAttribute.RESOLVED_ENDPOINT);
        if (resolvedEndpoint.headers().isEmpty()) {
            return context.httpRequest();
        }
        SdkHttpRequest.Builder httpRequestBuilder = context.httpRequest().toBuilder();
        resolvedEndpoint.headers().forEach((name, values) -> {
            values.forEach(v -> httpRequestBuilder.appendHeader(name, v));
        });
        return httpRequestBuilder.build();
    }

    public static QueryEndpointParams ruleParams(SdkRequest request, ExecutionAttributes executionAttributes) {
        QueryEndpointParams.Builder builder = QueryEndpointParams.builder();
        builder.region(AwsEndpointProviderUtils.regionBuiltIn(executionAttributes));
        builder.useDualStackEndpoint(AwsEndpointProviderUtils.dualStackEnabledBuiltIn(executionAttributes));
        builder.useFipsEndpoint(AwsEndpointProviderUtils.fipsEnabledBuiltIn(executionAttributes));
        builder.awsAccountId(AwsEndpointProviderUtils.accountIdBuiltIn(executionAttributes));
        setClientContextParams(builder, executionAttributes);
        setContextParams(builder, executionAttributes.getAttribute(AwsExecutionAttribute.OPERATION_NAME), request);
        setStaticContextParams(builder, executionAttributes.getAttribute(AwsExecutionAttribute.OPERATION_NAME));
        return builder.build();
    }

    private static void setContextParams(QueryEndpointParams.Builder params, String operationName, SdkRequest request) {
        switch (operationName) {
            case "OperationWithContextParam":
                setContextParams(params, (OperationWithContextParamRequest) request);
                break;
            default:
                break;
        }
    }

    private static void setContextParams(QueryEndpointParams.Builder params, OperationWithContextParamRequest request) {
        params.operationContextParam(request.stringMember());
    }

    private static void setStaticContextParams(QueryEndpointParams.Builder params, String operationName) {
        switch (operationName) {
            case "OperationWithStaticContextParams":
                operationWithStaticContextParamsStaticContextParams(params);
                break;
            default:
                break;
        }
    }

    private static void operationWithStaticContextParamsStaticContextParams(QueryEndpointParams.Builder params) {
        params.staticStringParam("hello");
    }

    private static void setClientContextParams(QueryEndpointParams.Builder params, ExecutionAttributes executionAttributes) {
        AttributeMap clientContextParams = executionAttributes.getAttribute(SdkInternalExecutionAttribute.CLIENT_CONTEXT_PARAMS);
        Optional.ofNullable(clientContextParams.get(QueryClientContextParams.BOOLEAN_CONTEXT_PARAM)).ifPresent(
            params::booleanContextParam);
        Optional.ofNullable(clientContextParams.get(QueryClientContextParams.STRING_CONTEXT_PARAM)).ifPresent(
            params::stringContextParam);
    }

    private static Optional<String> hostPrefix(String operationName, SdkRequest request) {
        switch (operationName) {
            case "APostOperation": {
                return Optional.of("foo-");
            }
            default:
                return Optional.empty();
        }
    }
}
