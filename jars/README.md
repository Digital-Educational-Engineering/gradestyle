# About
This folder contains 3 jars you can run, based on what study you are trying to replicate:

# GradeStyle-v001-2023-May-original-release.jar
This is the jar used for the original gradestyle paper (GradeStyle: GitHub-Integrated and Automated Assessment of Java Code Style).

To run this jar, run the following command:
```bash
> java -jar GradeStyle-v001-2023-May-original-release.jar <properties-file>
```

# GradeStyle-v003-2025-July-adoption-and-evolution-latest.jar
This is the jar for the newest version of GradeStyle, with all the new violations and some performance updates.

This version introduces a few new violations, including:
1. Missing Override Annotation
2. Unqualified Static Access
3. Empty Catch Block
4. Finalize Override

See section 6 of the Google Java Style Guide for more info 
https://google.github.io/styleguide/javaguide.html


To run this jar, run the following command:
```bash
> java -jar GradeStyle-v003-2025-July-adoption-and-evolution-latest.jar <properties-file>
```


# GradeStyle-v002-2025-February-adoption-and-evolution-experiment.jar
This is the jar used for replicating the results in the new paper (Adoption and Evolution of Code Style and Best Programming Practices in Open-Source Projects).

To run this jar, run the following command:
```bash
> java -jar GradeStyle-v002-2025-February-adoption-and-evolution-experiment.jar <properties-file>
```
The reason this jar is different to the newest jar is that certain violations were removed in this version, that were not investigated in the study (some ordering violations, and formatting violations for example).



