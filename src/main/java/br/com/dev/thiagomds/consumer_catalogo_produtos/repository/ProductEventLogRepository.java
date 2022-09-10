package br.com.dev.thiagomds.consumer_catalogo_produtos.repository;

import br.com.dev.thiagomds.consumer_catalogo_produtos.model.ProductEventKey;
import br.com.dev.thiagomds.consumer_catalogo_produtos.model.ProductEventLog;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan
public interface ProductEventLogRepository extends CrudRepository <ProductEventLog, ProductEventKey> {

    List<ProductEventLog> findAllByPk(String code);
    List<ProductEventLog> findAllByPkAndSkStartsWith(String code, String eventType);

}
