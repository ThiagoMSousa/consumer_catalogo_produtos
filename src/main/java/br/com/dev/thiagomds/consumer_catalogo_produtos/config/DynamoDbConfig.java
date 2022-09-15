package br.com.dev.thiagomds.consumer_catalogo_produtos.config;

import br.com.dev.thiagomds.consumer_catalogo_produtos.repository.ProductEventLogRepository;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.socialsignin.spring.data.dynamodb.repository.config.DynamoDBMapperConfigFactory;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableDynamoDBRepositories(basePackageClasses = ProductEventLogRepository.class)
@Profile("!local")
public class DynamoDbConfig {

    @Value("${aws.region}")
    private String awsRegion;

    // Configuração Padrão do DynamoMapper
    @Bean
    @Primary
    public DynamoDBMapperConfig dynamoDBMapperConfig() { return DynamoDBMapperConfig.DEFAULT; }

    @Bean
    @Primary
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB,
                                         DynamoDBMapperConfig config) {
        return new DynamoDBMapper(amazonDynamoDB, config);
    }

    // Cliente do DynamoDB
    @Bean
    @Primary
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
                // Definindo Credenciais
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                // Definindo Região
                .withRegion(Regions.fromName(awsRegion)).build();
    }
}
