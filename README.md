# Recursive Archive Extractor
Recursively copies/extracts all files/archives to a new identical (extracted) folder 

Scenario: You have a folder containing (optional) many subfolders, among the files in this folder tree there are also archive files, you want the same folder structure but with all of the archives extracted - each one to its own folder.

**Example**:  
*folder1 -> subfolder -> archive.zip*

After running RAE, a target folder will be created with the following tree structure:   
*folder1 -> subfolder -> archive (folder) -> extracted files.*

# RecursiveArchiveExtractor Features
- Source folder won't be modified, target folder will be created anew with the new folder tree.
- Non-archive files will be copied to the new structure
- Archives within archives will also be extracted
- Archive extraction supports: Zip, Tar, 7zip, Jar, Dump, CPIO, AR, ARJ

# How to Run ?
- [Download the latest released Jar file](https://github.com/Chaiavi/Recursive-Archive-Extractor/releases)
- Make sure you have JRE17 (download it if not)
- Run: **java -jar RecursiveArchiveExtractor-0.8.jar %SOURCE_FOLDER% %TARGET_FOLDER%**
