# file-archiver
A short groovy script that will rearrange all files in a directory tree
by moving them into year/month/day subtrees and zipping month folders as they get old.

## Command-line usage:

```bash
java -jar file-archiver.jar  path1, path2, ...
```

## Additional info

the utility supports two arguments: the reference date (files modified
after the reference date will be ignored) and the number of month
directories to keep unzipped (relative to the reference date).
For example if the reference date is June 1, 2015 and the number of
months is 2, the directories for April and May 2015 will not be zipped
