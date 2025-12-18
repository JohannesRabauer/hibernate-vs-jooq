package backend_jooq.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ApiNewCustomer(@NotBlank String firstName, @NotBlank String lastName, @Email String email) {
}

