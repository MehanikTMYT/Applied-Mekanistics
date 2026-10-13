# Applied Mekanistics: Version Locked

Community compatibility port of [Applied Mekanistics](https://github.com/AppliedEnergistics/Applied-Mekanistics) for Minecraft **26.1.2**, NeoForge **26.1.2.109**, AE2 **26.1.12-beta**, and Mekanism Version Locked **2.7**.

Use Mekanism and Mekanism Extras chemicals in ME storage, processing patterns, import/export buses, storage buses, interfaces, pattern providers, portable cells and chemical P2P tunnels. The JEI converter supports every registered chemical namespace, including the 15 Extras chemicals. AE2's native fluid converter handles the six Extras source fluids.

The chemical transport uses the modern NeoForge ResourceHandler/ItemAccess transaction API. Simulations roll back; reported accepted amounts equal actual inserted/extracted quantities, including large requests. Radioactive chemicals retain the original cell blacklist; processing patterns and suitable external storage remain separate from cell admission rules.

## Installation
Install the JAR on **both client and server**. Keep the same Mekanism and AE2 versions on both. JEI is needed on clients for recipe transfer and ghost ingredients. Mekanism Extras is optional. GuideME is an AE2 dependency. This build targets the ATM11 JEI setup; the original optional EMI integration is preserved in source but is not compiled for this pack.

## Validation
- Dedicated server startup with Mekanism, Generators, MoreMachine, Extras, AE2 and GuideME.
- Every Extras chemical: registry codec, processing pattern encoding/decoding, permitted cell storage persistence, native chemical tank simulation/commit/extraction.
- Client Minecraft and JEI startup: actual converter registration, all 15 chemical roundtrips, all six source fluids, key renderer and modern item model resources.
- Chemical P2P initializes the native transactional handler. The current checks do not simulate a complete multiblock ME network or player-operated remote server.

## Building
Java 25 and the included Gradle 9.6.1 wrapper. Put the dependency JARs listed in build.gradle into the ignored libs/ directory, then run `gradlew.bat jar`. No dependency mods are embedded in the output.

## Credits and license
Original authors: **ramidzkh and the AppliedEnergistics contributors**. This port is not endorsed by the original developers. Code retains **LGPL-3.0**; original assets retain **CC BY-NC-SA 3.0**, as stated by the upstream repository. AE2 renderer adaptation retains its LGPL attribution in THIRD-PARTY-NOTICES.md. Original repository history is retained in this checkout.
