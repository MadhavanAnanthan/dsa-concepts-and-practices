# Books Directory

Create one folder per book alias:

```text
books/
  DDIA/
    raw/
      chapter-05-replication.md
    notes/
      chapter-05-replication-compressed-study-notes.md
    summaries/
      chapter-05-replication-summary.md
  KAFKA/
    raw/
    summaries/
```

Use short uppercase folder names because the CLI commands use the folder name as the book alias:

```text
/DDIA explain quorum writes
/KAFKA explain ISR
```

PDF files can be placed in the matching book folder, but this first version indexes markdown files only. Convert PDFs into markdown chapter files before indexing.

For new compressed study files, include both the chapter number and the chapter title in the filename so the file is understandable without opening it.
