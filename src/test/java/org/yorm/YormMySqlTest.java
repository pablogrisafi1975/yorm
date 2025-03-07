package org.yorm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.yorm.exception.YormException;
import org.yorm.records.*;
import org.yorm.util.DbType;
import org.yorm.utils.TestConnectionFactory;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class YormMySqlTest {

    private static DataSource ds;
    private static Yorm yorm;

    @BeforeAll
    private static void initDb() {
        ds = TestConnectionFactory.getMySqlConnection();
        yorm = new Yorm(ds);
    }

    @Test
    @Order(1)
    void buildMap() throws YormException {
        MapBuilder mp = new MapBuilder(ds);
        YormTable map = mp.buildMap(Person.class);
        assertNotNull(map);
        List<YormTuple> tuples = map.tuples();
        assertEquals(5, tuples.size());
        YormTuple tuple0 = tuples.get(0);
        assertEquals("id", tuple0.dbFieldName());
        assertEquals(DbType.INTEGER, tuple0.type());
        assertEquals("id", tuple0.method().getName());
        assertEquals("id", tuple0.objectName());
        assertTrue(tuple0.isPrimaryKey());
        YormTuple tuple3 = tuples.get(3);
        assertEquals("last_login", tuple3.dbFieldName());
        assertEquals(DbType.TIMESTAMP, tuple3.type());
        assertEquals("lastLogin", tuple3.method().getName());
        assertEquals("lastLogin", tuple3.objectName());
        assertEquals("last_login", tuple3.dbFieldName());
        assertFalse(tuple3.isPrimaryKey());
        YormTuple tuple4 = tuples.get(4);
        assertEquals("company_id", tuple4.dbFieldName());
        assertEquals(DbType.INTEGER, tuple0.type());
        assertEquals("companyId", tuple4.method().getName());
        assertEquals("companyId", tuple4.objectName());
        assertEquals("company_id", tuple4.dbFieldName());
        assertFalse(tuple4.isPrimaryKey());
    }

    @Test
    @Order(2)
    void saveCompany() throws YormException {
        Company company = new Company(0, "Hogwarts", "GB", LocalDate.of(1968, 2, 12), 154.1f, true);
        long id = yorm.save(company);
        assertEquals(1, id);
        Company company2 = new Company(0, "Mordor", "ZZ", LocalDate.of(114, 11, 5), 0f, false);
        long id2 = yorm.save(company2);
        assertEquals(2, id2);
    }

    @Test
    @Order(3)
    void getCompany() throws YormException {
        Company company = yorm.find(Company.class, 1);
        assertEquals("GB", company.countryCode());
        assertEquals("Hogwarts", company.name());
        assertEquals(LocalDate.of(1968, 2, 12), company.date());
        assertEquals(154.1f, company.debt());
        assertTrue(company.isActive());

    }

    @Test
    @Order(4)
    void savePerson() throws YormException {
        Person person = new Person(0, "John", "john.doe@um.com", LocalDateTime.of(2022, 3, 22, 11, 14, 13), 1);
        long idPerson = yorm.save(person);
        assertEquals(1, idPerson);
        Person personWrong = new Person(0, "John", "john.doe@um.com", LocalDateTime.of(2022, 1, 15, 7, 53, 21), 0);
        assertThrows(YormException.class, () -> yorm.save(personWrong));
    }

    @Test
    @Order(5)
    void saveListPersons() throws YormException {
        LocalDateTime localDateTime = LocalDateTime.now();
        Person person1 = new Person(2, "Hermione", "hermione.granger@hogwarts.com", localDateTime, 1);
        Person person2 = new Person(3, "Harry", "harry.potter@hogwarts.com", localDateTime, 1);
        Person person3 = new Person(4, "Sauron", "sauron@mordor.com", localDateTime, 2);
        List<Person> list = List.of(person1, person2, person3);
        yorm.insert(list);
        List<Person> personList = yorm.find(Person.class);
        assertNotNull(personList);
        assertEquals(4, personList.size());
    }

    @Test
    @Order(6)
    void getPerson() throws YormException {
        Person person = yorm.find(Person.class, 1);
        assertEquals("John", person.name());
        assertEquals("john.doe@um.com", person.email());
        assertEquals(1, person.companyId());
        Person person2 = yorm.find(Person.class, 2);
        assertEquals("Hermione", person2.name());
        assertEquals("hermione.granger@hogwarts.com", person2.email());
        assertEquals(1, person2.companyId());
    }

    @Test
    @Order(7)
    void getEverything() throws YormException {
        List<Person> personList = yorm.find(Person.class);
        assertNotNull(personList);
        assertEquals(4, personList.size());
        Person person2 = personList.get(1);
        assertEquals("Hermione", person2.name());
        assertEquals("hermione.granger@hogwarts.com", person2.email());
        assertEquals(1, person2.companyId());
    }

    @Test
    @Order(8)
    void getWithForeignKey() throws YormException {
        Company company = new Company(1, null, null, null, 0, false);
        List<Person> personList = yorm.find(Person.class, company);
        assertNotNull(personList);
        assertEquals(3, personList.size());
        Person person2 = personList.get(1);
        assertEquals("Hermione", person2.name());
        assertEquals("hermione.granger@hogwarts.com", person2.email());
        assertEquals(1, person2.companyId());
        List<Person> personList2 = yorm.find(Person.class, new Company(2, null, null, null, 0, false));
        assertNotNull(personList2);
        assertEquals(1, personList2.size());
        Person person1 = personList2.get(0);
        assertEquals("Sauron", person1.name());
        assertEquals("sauron@mordor.com", person1.email());
        assertEquals(2, person1.companyId());
    }

    @Test
    @Order(9)
    void getFiltering() throws YormException {
        Person personFilter1 = new Person(0, "harry", "john", null, 0);
        Person personFilter2 = new Person(0, null, null, null, 2);
        List<Person> list = List.of(personFilter1, personFilter2);
        List<Person> personList = yorm.find(list);
        assertNotNull(personList);
        assertEquals(3, personList.size());
        Person person1 = personList.stream().filter(p -> p.id() == 4).findFirst().get();
        assertEquals(4, person1.id());
        assertEquals("Sauron", person1.name());
        assertEquals("sauron@mordor.com", person1.email());
        assertEquals(2, person1.companyId());
        List<Person> list2 = yorm.from(Person.class).where(Person::email).like(".com").find();
        assertFalse(list2.isEmpty());
    }

    @Test
    @Order(10)
    void getFilteringNoResults() throws YormException {
        Person personFilter1 = new Person(0, "Peter", null, null, 0);
        List<Person> personList = yorm.find(List.of(personFilter1));
        assertTrue(personList.isEmpty());
    }

    @Test
    @Order(11)
    void update() throws YormException {
        LocalDateTime localDateTime = LocalDateTime.of(2022, 1, 12, 14, 32, 14);
        Person personResultBefore = yorm.find(Person.class, 1);
        assertEquals(1, personResultBefore.id());
        assertEquals("John", personResultBefore.name());
        assertEquals("john.doe@um.com", personResultBefore.email());
        assertEquals(1, personResultBefore.companyId());
        Person person = new Person(1, "Draco", "draco.malfoy@hogwarts.com", localDateTime, 1);
        yorm.save(person);//Update because the id is 1
        Person personResult = yorm.find(Person.class, 1);
        assertEquals(1, personResult.id());
        assertEquals("Draco", personResult.name());
        assertEquals("draco.malfoy@hogwarts.com", personResult.email());
        assertEquals(1, personResult.companyId());
        Person person2 = new Person(1, "Lucius", "lucius.malfoy@hogwarts.com", localDateTime, 1);
        yorm.update(person2);
        Person personResult2 = yorm.find(Person.class, 1);
        assertEquals(1, personResult2.id());
        assertEquals("Lucius", personResult2.name());
        assertEquals("lucius.malfoy@hogwarts.com", personResult2.email());
    }

    @Test
    @Order(12)
    void delete() throws YormException {
        var somethingWasDeleted = yorm.delete(Person.class, 1);
        assertTrue(somethingWasDeleted);
        Person personResult = yorm.find(Person.class, 1);
        assertNull(personResult);
        somethingWasDeleted = yorm.delete(Person.class, 1);
        assertFalse(somethingWasDeleted);
    }

    @Test
    @Order(13)
    void mapWrongRecord() {
        Invoice invoice = new Invoice(1, 1);
        assertThrows(YormException.class, () -> yorm.insert(invoice));
    }

    @Test
    @Order(14)
    void testFluentWhere() throws YormException {
        List<Person> list = yorm.from(Person.class).where(Person::email).like(".com").find();
        assertFalse(list.isEmpty());
        List<Person> secondList = yorm.from(Person.class).where(Person::companyId).equalTo(100)
            .or(Person::email).like("hogwarts")
            .find();
        assertEquals(2, secondList.size());
        List<Person> thirdList = yorm.from(Person.class).where(Person::name).equalTo("Hermione")
            .and(Person::lastLogin).greaterThan(LocalDateTime.of(2019, 01, 01, 0, 0, 0))
            .find();
        assertEquals(1, thirdList.size());
        List<Company> companyList = yorm.from(Company.class).where(Company::isActive).notEqualTo(false).find();
        assertEquals(1, companyList.size());
    }

}
