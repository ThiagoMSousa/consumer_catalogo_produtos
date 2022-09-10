package br.com.dev.thiagomds.consumer_catalogo_produtos.service;

import br.com.dev.thiagomds.consumer_catalogo_produtos.model.Envelope;
import br.com.dev.thiagomds.consumer_catalogo_produtos.model.ProductEvent;
import br.com.dev.thiagomds.consumer_catalogo_produtos.model.SnsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;

@Service
public class ProductEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(
            ProductEventConsumer.class
    );
    private ObjectMapper objectMapper;

    // Mapper para efetuar a deserialização das mensagens
    @Autowired
    public ProductEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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

        log.info("Product event received - Event: {} - ProductId: {} - ",
                envelope.getEventType(),
                productEvent.getProductId());


    }

}
