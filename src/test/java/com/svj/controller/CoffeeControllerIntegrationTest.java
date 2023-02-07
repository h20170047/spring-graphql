package com.svj.controller;

import com.svj.entity.Coffee;
import com.svj.entity.Size;
import com.svj.service.CoffeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.Optional;

import static com.svj.entity.Size.GRANDE;
import static com.svj.entity.Size.VENTI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Import(CoffeeService.class)
@GraphQlTest(CoffeeController.class)
class CoffeeControllerIntegrationTest {

    @Autowired
    GraphQlTester graphQlTester;

    @Autowired
    CoffeeService coffeeService;

    @Test
    @DisplayName("Checks if all coffee is retrieved")
    void testFindAll(){
        // language=GRAPHQL
        String document= """
                query {
                    findAll {
                        id
                        name
                        size                    
                    }
                }
                """;
        graphQlTester.document(document)
                .execute()
                .path("findAll")
                .entityList(Coffee.class)
                .hasSize(3);
    }

    @Test
    @DisplayName("Checks if valid ID returns coffee instance")
    void testFindOne() {
        // language=GRAPHQL
        String document = """
            query findOne($id: ID){
                findOne(id: $id){
                    id
                    name
                    size
                }
            }
        """;
        graphQlTester.document(document)
                .variable("id", 1)
                .execute()
                .path("findOne")
                .entity(Coffee.class)
                .satisfies(coffee -> {
                    assertEquals("Caffè Americano", coffee.name());
                    assertEquals(GRANDE, coffee.size());
                });
    }

    @Test
    @DisplayName("Should be able to create a new Coffee")
    public void testCreate(){
        int coffeCount= coffeeService.findAll().size();
        // language=GRAPHQL
        String document= """
                mutation create($name: String, $size: Size){
                    create(name: $name, size: $size){
                        id
                        name
                        size
                    }
                }
        """;
        graphQlTester.document(document)
                .variable("name", "Caffee Latte")
                .variable("size", Size.VENTI)
                .execute()
                .path("create")
                .entity(Coffee.class)
                .satisfies(coffee -> {
                    assertNotNull(coffee.id());
                    assertEquals("Caffee Latte", coffee.name());
                    assertEquals(VENTI, coffee.size());
                });
        assertThat(coffeCount+1).isEqualTo(coffeeService.findAll().size());
    }

    @Test
    @DisplayName("Should be able to update a coffee entry")
    public void testUpdate(){
        // language=GRAPHQL
        String doucment= """
                mutation update($id: ID, $name: String, $size: Size){
                    update(id: $id, name: $name, size: $size){
                        id
                        name
                        size
                    }
                }
                """;
        graphQlTester.document(doucment)
                .variable("id", 1)
                .variable("name", "Espresso Coffee")
                .variable("size", VENTI)
                .execute()
                .path("update")
                .entity(Coffee.class)
                .satisfies(coffee -> {
                    assertNotNull(coffee.id());
                    assertEquals("Espresso Coffee", coffee.name());
                    assertEquals(VENTI, coffee.size());
                });
    }

    @Test
    @DisplayName("Should be able to delete a coffee entry")
    public void testDelete(){
        Optional<Coffee> coffee = coffeeService.findOne(1);
        assertTrue(coffee.isPresent());
        // language=GRAPHQL
        String document= """
                mutation delete($id: ID){
                    delete(id: $id){
                        id
                        name
                        size
                    }
                }
                """;
        graphQlTester.document(document)
                .variable("id", 1)
                .executeAndVerify();
        assertFalse(coffeeService.findOne(1).isPresent());
    }
}
