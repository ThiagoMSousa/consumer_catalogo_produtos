package br.com.dev.thiagomds.consumer_catalogo_produtos.service;

import br.com.dev.thiagomds.consumer_catalogo_produtos.model.Envelope;
import br.com.dev.thiagomds.consumer_catalogo_produtos.model.ProductEvent;
import br.com.dev.thiagomds.consumer_catalogo_produtos.model.ProductEventLog;
import br.com.dev.thiagomds.consumer_catalogo_produtos.model.SnsMessage;
import br.com.dev.thiagomds.consumer_catalogo_produtos.repository.ProductEventLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Service
public class ProductEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(
            ProductEventConsumer.class
    );
    private ObjectMapper objectMapper;
    private ProductEventLogRepository productEventLogRepository;

    // Mapper para efetuar a deserialização das mensagens
    @Autowired
    public ProductEventConsumer(ObjectMapper objectMapper, ProductEventLogRepository productEventLogRepository) {
        this.objectMapper = objectMapper;
        this.productEventLogRepository = productEventLogRepository;
    }

    // Essa anotação, informa que este método sera invocado quando chegar uma mensagem na Fila
    @JmsListener(destination = "${aws.sqs.queue.product.events.name}")
    public void receiveProductEvent(TextMessage textMessage) throws JMSException, IOException {

        // Deserializa o Texto da Mensagem e Converte no Modelo SnsMessage
        SnsMessage snsMessage = objectMapper.readValue(textMessage.getText(),
                SnsMessage.class);

        // Deserializa a mensagem do SNS e converte no modelo Envelope
        Envelope envelope = objectMapper.readValue(snsMessage.getMessage(),
                Envelope.class);

        // Obtendo o evento gerado através do Envelope
        ProductEvent productEvent = objectMapper.readValue(envelope.getData(), ProductEvent.class);

        log.info("Product event received - Event: {} - ProductId: {} - MessageId: {}",
                envelope.getEventType(),
                productEvent.getProductId(),
                snsMessage.getMessageId());

        ProductEventLog productEventLog = buildProductEventLog(envelope, productEvent);
        productEventLogRepository.save(productEventLog);
    }

    private ProductEventLog buildProductEventLog(Envelope envelope,
                                                 ProductEvent productEvent) {
        long timestamp = Instant.now().toEpochMilli();

        ProductEventLog productEventLog = new ProductEventLog();
        productEventLog.setPk(productEvent.getCode());
        productEventLog.setSk(envelope.getEventType() + "_" + timestamp);
        productEventLog.setEventType(envelope.getEventType());
        productEventLog.setProductId(productEvent.getProductId());
        productEventLog.setUsername(productEventLog.getUsername());
        productEventLog.setTimestamp(timestamp);
        productEventLog.setTtl(Instant.now().plus(
                Duration.ofMinutes(10)).getEpochSecond());

        return productEventLog;
    }

}
