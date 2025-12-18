package backend_jooq.repositories;

import backend_jooq.db.dtos.DbCustomer;
import backend_jooq.model.Customer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class CustomerRepository {

    private final EntityManager em;

    public CustomerRepository(EntityManager em) {
        this.em = em;
    }

    public List<DbCustomer> findAll() {
        return em.createQuery("SELECT c FROM Customer c", Customer.class)
                .getResultList()
                .stream()
                .map(c -> new DbCustomer(c.getCustomerId(), c.getFirstName(), c.getLastName(), c.getEmail()))
                .toList();
    }

    public DbCustomer create(String firstName, String lastName, String email) {
        Customer c = new Customer(firstName, lastName, email);
        em.persist(c);
        em.flush();
        return new DbCustomer(c.getCustomerId(), c.getFirstName(), c.getLastName(), c.getEmail());
    }
}
