/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.customers.web;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Optional;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Maciej Szarlinski
 */
@RequestMapping("/owners")
@RestController
@Timed("petclinic.owner")
@RequiredArgsConstructor
@Slf4j
class OwnerResource {

    private final OwnerRepository ownerRepository;

    /**
     * Create a new owner.
     * Validates the owner data and persists it to the database.
     *
     * @param owner the owner data to create
     * @return the created owner with generated ID
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Owner createOwner(@Valid @RequestBody Owner owner) {
        return ownerRepository.save(owner);
    }

    /**
     * Retrieve a single owner by ID.
     * Returns an Optional to allow the caller to handle the not-found case.
     *
     * @param ownerId the ID of the owner to retrieve (must be at least 1)
     * @return an Optional containing the owner if found, empty otherwise
     */
    @GetMapping(value = "/{ownerId}")
    public Optional<Owner> findOwner(@PathVariable("ownerId") @Min(1) int ownerId) {
        return ownerRepository.findById(ownerId);
    }

    /**
     * Retrieve all owners in the system.
     * Returns the complete list of owners with their associated pets.
     *
     * @return list of all owners
     */
    @GetMapping
    public List<Owner> findAll() {
        return ownerRepository.findAll();
    }

    /**
     * Update an existing owner's information.
     * All fields in the owner request will overwrite the existing owner data.
     * This uses a manual field-by-field update approach for simplicity.
     * Note: In production, consider using MapStruct for automatic mapping.
     *
     * @param ownerId the ID of the owner to update
     * @param ownerRequest the updated owner data
     * @throws ResourceNotFoundException if the owner doesn't exist
     */
    @PutMapping(value = "/{ownerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateOwner(@PathVariable("ownerId") @Min(1) int ownerId, @Valid @RequestBody Owner ownerRequest) {
        final Optional<Owner> owner = ownerRepository.findById(ownerId);
        final Owner ownerModel = owner.orElseThrow(() -> new ResourceNotFoundException("Owner "+ownerId+" not found"));

        // This is done by hand for simplicity purpose. In a real life use-case we should consider using MapStruct.
        ownerModel.setFirstName(ownerRequest.getFirstName());
        ownerModel.setLastName(ownerRequest.getLastName());
        ownerModel.setCity(ownerRequest.getCity());
        ownerModel.setAddress(ownerRequest.getAddress());
        ownerModel.setTelephone(ownerRequest.getTelephone());
        log.info("Saving owner {}", ownerModel);
        ownerRepository.save(ownerModel);
    }
}
