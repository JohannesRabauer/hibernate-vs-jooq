package backend_jooq.repositories;

import backend_jooq.db.dtos.DbAddress;
import backend_jooq.model.Address;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;


@ApplicationScoped
public class AddressRepository implements PanacheRepository<Address> {


    public List<DbAddress> findAllByCustomerId(int customerId) {
        return list("customer.id", customerId)
                .stream()
                .map(address ->
                        new DbAddress(address.getStreet(), address.getCity(), address.getCountry())
                ).toList();
    }
}
