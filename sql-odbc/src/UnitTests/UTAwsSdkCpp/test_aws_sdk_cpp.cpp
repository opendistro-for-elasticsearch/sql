// clang-format off
#include "pch.h"
#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif  // __APPLE__
#include <aws/core/Aws.h>
#include <aws/core/auth/AWSAuthSigner.h>
#include <aws/core/auth/AWSCredentialsProvider.h>
#include <aws/core/http/HttpClient.h>
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif  // __APPLE__
#include "unit_test_helper.h"

using namespace Aws::Auth;
using namespace Aws::Client;
using namespace Aws::Http;

static const char service_name[] = "es";
static const char allocation_tag[] = "AWS_SIGV4_Test";
static const char host[] = "https://search-bit-quill-cx3hpfoxvasohujxkllmgjwqde.us-west-2.es.amazonaws.com";
static const char region[] = "us-west-2";

TEST(AWS_SIGV4, EnvironmentAWSCredentials) {
	Aws::SDKOptions options;
	EXPECT_NO_THROW(Aws::InitAPI(options));

	auto request = CreateHttpRequest(Aws::String(host), HttpMethod::HTTP_GET, Aws::Utils::Stream::DefaultResponseStreamFactoryMethod);

	std::shared_ptr<EnvironmentAWSCredentialsProvider> credential_provider =  Aws::MakeShared<Aws::Auth::EnvironmentAWSCredentialsProvider>(allocation_tag);

	AWSAuthV4Signer signer(credential_provider, service_name, region);
	ASSERT_TRUE(signer.SignRequest(*request));

	auto http_client = CreateHttpClient(Aws::Client::ClientConfiguration());

	auto response = http_client->MakeRequest(request);
	ASSERT_NE(response, nullptr);
	EXPECT_EQ(Aws::Http::HttpResponseCode::OK, response->GetResponseCode());
	
	EXPECT_NO_THROW(Aws::ShutdownAPI(options));
}

TEST(SettingSDKOptions, TurnLoggingOn) {
    Aws::SDKOptions options;
	options.loggingOptions.logLevel = Aws::Utils::Logging::LogLevel::Info;
    EXPECT_NO_THROW(Aws::InitAPI(options));
    EXPECT_NO_THROW(Aws::ShutdownAPI(options));
}

int main(int argc, char** argv) {
    testing::internal::CaptureStdout();
    ::testing::InitGoogleTest(&argc, argv);
    int failures = RUN_ALL_TESTS();
    std::string output = testing::internal::GetCapturedStdout();
    std::cout << output << std::endl;
    std::cout << (failures ? "Not all tests passed." : "All tests passed") << std::endl;
    WriteFileIfSpecified(argv, argv + argc, "-fout", output);
    return failures;
}
