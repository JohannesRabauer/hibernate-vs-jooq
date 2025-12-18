package backend_jooq.repositories;

import backend_jooq.db.dtos.DbAddress;
import backend_jooq.model.Address;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;


@ApplicationScoped
public class AddressRepository {

    private final EntityManager em;

    public AddressRepository(EntityManager em) {
        this.em = em;
    }

    public List<DbAddress> findAllByCustomerId(int customerId) {
        return em.createQuery("SELECT a FROM Address a WHERE a.customer.customerId = :cid", Address.class)
                .setParameter("cid", customerId)
                .getResultList()
                .stream()
                .map(a -> new DbAddress(a.getStreet(), a.getCity(), a.getCountry()))
                .toList();
    }
}
