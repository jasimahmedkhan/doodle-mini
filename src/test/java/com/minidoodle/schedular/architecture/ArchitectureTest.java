package com.minidoodle.schedular.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static final String ROOT = "com.minidoodle.schedular.";
    private static final List<String> MODULES = List.of("slot", "meeting", "availability", "user");

    private static JavaClasses classes;

    @BeforeAll
    static void importProductionClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.minidoodle.schedular");
    }

    @Test
    void domainPackagesHaveNoSpringOrJpaDependencies() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence.."
                )
                .check(classes);
    }

    @Test
    void modulesDoNotAccessAnotherModulesApiOrInfrastructure() {
        for (String targetModule : MODULES) {
            String[] originPackages = MODULES.stream()
                    .filter(module -> !module.equals(targetModule))
                    .map(module -> ROOT + module + "..")
                    .toArray(String[]::new);

            noClasses()
                    .that().resideInAnyPackage(originPackages)
                    .should().dependOnClassesThat().resideInAnyPackage(
                            ROOT + targetModule + ".api..",
                            ROOT + targetModule + ".infrastructure.."
                    )
                    .check(classes);
        }
    }

    @Test
    void meetingAccessesSlotOnlyThroughApplicationContracts() {
        noClasses()
                .that().resideInAPackage(ROOT + "meeting..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        ROOT + "slot.domain..",
                        ROOT + "slot.api..",
                        ROOT + "slot.infrastructure.."
                )
                .check(classes);
    }

    @Test
    void availabilityAccessesSlotOnlyThroughApplicationContracts() {
        noClasses()
                .that().resideInAPackage(ROOT + "availability..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        ROOT + "slot.domain..",
                        ROOT + "slot.api..",
                        ROOT + "slot.infrastructure.."
                )
                .check(classes);
    }

    @Test
    void sharedDomainDoesNotDependOnFeatureModules() {
        noClasses()
                .that().resideInAPackage(ROOT + "shared.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        ROOT + "slot..",
                        ROOT + "meeting..",
                        ROOT + "availability..",
                        ROOT + "user.."
                )
                .check(classes);
    }
}
