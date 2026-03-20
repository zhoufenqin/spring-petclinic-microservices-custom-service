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
import org.springframework.samples.petclinic.customers.model.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Optional;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 * @author Ramazan Sakin
 */
@RestController
@Timed("petclinic.pet")
@RequiredArgsConstructor
@Slf4j
class PetResource {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;


    /**
     * Retrieve all available pet types.
     * Used by clients to populate pet type selection options.
     *
     * @return list of all pet types
     */
    @GetMapping("/petTypes")
    public List<PetType> getPetTypes() {
        return petRepository.findPetTypes();
    }

    /**
     * Create a new pet for an owner.
     * The pet is automatically associated with the owner and saved to the database.
     *
     * @param petRequest the pet details to create
     * @param ownerId the ID of the owner who will own this pet
     * @return the created pet entity with generated ID
     * @throws ResourceNotFoundException if the owner doesn't exist
     */
    @PostMapping("/owners/{ownerId}/pets")
    @ResponseStatus(HttpStatus.CREATED)
    public Pet processCreationForm(
        @RequestBody PetRequest petRequest,
        @PathVariable("ownerId") @Min(1) int ownerId) {

        final Optional<Owner> optionalOwner = ownerRepository.findById(ownerId);
        Owner owner = optionalOwner.orElseThrow(() -> new ResourceNotFoundException("Owner "+ownerId+" not found"));

        final Pet pet = new Pet();
        owner.addPet(pet);
        return save(pet, petRequest);
    }

    /**
     * Update an existing pet's information.
     * Uses wildcard (*) in path to accept any owner ID for flexibility.
     *
     * @param petRequest the updated pet details including the pet ID
     * @throws ResourceNotFoundException if the pet doesn't exist
     */
    @PutMapping("/owners/*/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processUpdateForm(@RequestBody PetRequest petRequest) {
        int petId = petRequest.getId();
        Pet pet = findPetById(petId);
        save(pet, petRequest);
    }

    /**
     * Helper method to save or update a pet entity with data from a request.
     * This method handles the mapping between PetRequest DTO and Pet entity.
     *
     * @param pet the pet entity to update
     * @param petRequest the request containing updated pet information
     * @return the saved pet entity
     */
    private Pet save(final Pet pet, final PetRequest petRequest) {

        pet.setName(petRequest.getName());
        pet.setBirthDate(petRequest.getBirthDate());

        petRepository.findPetTypeById(petRequest.getTypeId())
            .ifPresent(pet::setType);

        log.info("Saving pet {}", pet);
        return petRepository.save(pet);
    }

    /**
     * Retrieve details of a specific pet.
     * Returns a DTO with formatted owner information.
     *
     * @param petId the ID of the pet to retrieve
     * @return pet details including owner name
     * @throws ResourceNotFoundException if the pet doesn't exist
     */
    @GetMapping("owners/*/pets/{petId}")
    public PetDetails findPet(@PathVariable("petId") int petId) {
        return new PetDetails(findPetById(petId));
    }


    /**
     * Helper method to find a pet by ID.
     * Uses modern Optional.orElseThrow() pattern for cleaner exception handling.
     *
     * @param petId the ID of the pet to find
     * @return the pet entity
     * @throws ResourceNotFoundException if the pet doesn't exist
     */
    private Pet findPetById(int petId) {
        return petRepository.findById(petId)
            .orElseThrow(() -> new ResourceNotFoundException("Pet "+petId+" not found"));
    }

}
