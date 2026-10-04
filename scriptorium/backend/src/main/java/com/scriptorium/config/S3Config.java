package com.scriptorium.config;

import org.springframework.context.annotation.Configuration;

/**
 * Placeholder for future S3 / object-storage configuration.
 *
 * When moving from local file storage to S3:
 *  1. Add the AWS SDK dependency to pom.xml.
 *  2. Add AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION, S3_BUCKET to .env.
 *  3. Uncomment and populate the S3Client bean below.
 *
 * <pre>
 * {@code
 * @Bean
 * public S3Client s3Client(
 *         @Value("${AWS_REGION}") String region,
 *         @Value("${AWS_ACCESS_KEY_ID}") String accessKey,
 *         @Value("${AWS_SECRET_ACCESS_KEY}") String secretKey) {
 *
 *     return S3Client.builder()
 *             .region(Region.of(region))
 *             .credentialsProvider(StaticCredentialsProvider.create(
 *                     AwsBasicCredentials.create(accessKey, secretKey)))
 *             .build();
 * }
 * }
 * </pre>
 *
 * The {@code contentUrl} field on ManuscriptNode is already S3-ready — just
 * store the full object URL (e.g. {@code https://s3.amazonaws.com/bucket/key})
 * and serve it directly from the frontend.
 */
@Configuration
public class S3Config {
    // No beans needed while using local file storage.
}
