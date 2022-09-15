package br.com.dev.thiagomds.consumer_catalogo_produtos.config.local;

import br.com.dev.thiagomds.consumer_catalogo_produtos.repository.ProductEventLogRepository;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;


@Configuration
@EnableDynamoDBRepositories(basePackageClasses = ProductEventLogRepository.class)
@Profile("local")
public class DynamoDBConfigLocal {

    private static final Logger LOG = LoggerFactory.getLogger(DynamoDBConfigLocal.class);
    private final AmazonDynamoDB amazonDynamoDB;


    public DynamoDBConfigLocal() {
        this.amazonDynamoDB = AmazonDynamoDBClient.builder()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("http://localhost:4566",
                                Regions.US_EAST_1.getName())
                )
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        // Criando a instância do DynamoDB
        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);

        // Cliente do DynamoDB precisa de uma lista de atributos
        // Criando a Lista de Atríbutos
        List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();

        // Criando o atríbuto da chave 'Partition Key'
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("pk")
                // Definindo o tipo do atríbuto
                // ScalarAttributeType.S = STRING
                .withAttributeType(ScalarAttributeType.S));

        // Criando o atríbuto da chave 'Sort Key'
        attributeDefinitions.add(new AttributeDefinition().withAttributeName("sk")
                .withAttributeType(ScalarAttributeType.S));

        // Criando a Lista de Definição de Schema dos Atríbutos
        List<KeySchemaElement> keySchemaElements = new ArrayList<KeySchemaElement> ();
        keySchemaElements.add(new KeySchemaElement().withAttributeName("pk")
                .withKeyType(KeyType.HASH)); // HashKey

        keySchemaElements.add(new KeySchemaElement().withAttributeName("sk")
                .withKeyType(KeyType.RANGE)); // RangeKey

        // Criando o TableRequest que de fato, vai ser utilizado na criação da tabela
        CreateTableRequest createTableRequest = new CreateTableRequest()
                .withTableName("product-events") // Criando nome da tabela
                .withKeySchema(keySchemaElements) // Criando o Schema da tabela
                .withAttributeDefinitions(attributeDefinitions) // Lista de Definição dos Atríbutos
                .withBillingMode(BillingMode.PAY_PER_REQUEST); // Definindo o modo de cobrança

        // Solicitando a criação da tabela
        Table table = dynamoDB.createTable(createTableRequest);

        try {
            // Esperando pela criação da tabela
            table.waitForActive();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }

    }

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
        return this.amazonDynamoDB;
    }
}
