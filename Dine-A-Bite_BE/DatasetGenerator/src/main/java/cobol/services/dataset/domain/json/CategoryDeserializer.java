package cobol.services.dataset.domain.json;

import cobol.services.dataset.domain.SpringContext;
import cobol.services.dataset.domain.entity.Category;
import cobol.services.dataset.domain.repository.CategoryRepository;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CategoryDeserializer extends JsonDeserializer<Category> {


    @Override
    public Category deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String name = p.getText();
        CategoryRepository categoryRepository = SpringContext.getBean(CategoryRepository.class);
        return categoryRepository.save(new Category(name));
    }
}
