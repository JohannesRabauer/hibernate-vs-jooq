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
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Address.class);
        var root = cq.from(Address.class);
        cq.select(root).where(cb.equal(root.get("customer").get("customerId"), customerId));
        List<Address> results = em.createQuery(cq).getResultList();
        return results.stream()
                .map(a -> new DbAddress(a.getStreet(), a.getCity(), a.getCountry()))
                .toList();
    }
}
