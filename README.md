# Mule PDF Module

The Mule PDF Module is a MuleSoft extension that provides PDF manipulation capabilities within Mule applications. This module leverages Apache PDFBox and OpenHTMLToPDF libraries to offer powerful PDF operations without requiring external services.

## Features

The PDF module provides the following operations:

### Split Document

Splits a PDF document into multiple parts with configurable page distribution.

**Parameters:**
- **Original PDF Payload**: The PDF document to split (binary content)
- **Original File Name**: The file name of the PDF document
- **Split Label Suffix**: Text to append to each split part (default: "Part")
- **File Name and Suffix Separator**: Character(s) to separate filename and suffix (default: "_")
- **Start Parts with First Pages**: Number of pages from the original document to include at the beginning of each part
- **Max Number of Pages in Each Part**: Maximum number of pages per split document

**Output:**
- Returns a paging provider that yields each split PDF document with associated metadata

### Extract Pages

Extracts specific pages from a PDF document based on a page range specification.

**Parameters:**
- **Original PDF Payload**: The PDF document to extract pages from (binary content)
- **Original File Name**: The file name of the PDF document
- **Extract Pages**: Page ranges to extract using 1-based indexing (e.g., "3-6,8,10-13")

**Output:**
- Returns the extracted pages as a new PDF document with metadata

### HTML to PDF

Converts HTML content to a PDF document.

**Parameters:**
- **HTML Payload**: The HTML content to convert to PDF
- **Output File Name**: The file name for the generated PDF

**Output:**
- Returns the generated PDF document with metadata

## Installation

Add this dependency to your application's pom.xml:

```xml
<dependency>
    <groupId>org.mule.modules</groupId>
    <artifactId>mule-pdf-module</artifactId>
    <version>0.2.6</version>
    <classifier>mule-plugin</classifier>
</dependency>
```

## Usage Examples

### Split Document Example

```xml
<pdf:split-document 
    fileName="large-document.pdf" 
    splitLabel="Part" 
    labelSeparator="_" 
    maxPages="10" 
    firstPages="1"/>

<foreach collection="#[payload]">
    <file:write path="#[vars.outputPath ++ '/' ++ attributes.fileName]"/>
</foreach>
```

This example splits a PDF document into parts with 10 pages each, including the first page in each part.

### Extract Pages Example

```xml
<pdf:extract-pages 
    fileName="document.pdf" 
    extractPages="1-3,5,7-9"/>

<file:write path="#[vars.outputPath ++ '/' ++ attributes.fileName]"/>
```

This example extracts pages 1, 2, 3, 5, 7, 8, and 9 from the document and creates a new PDF.

### HTML to PDF Example

```xml
<parse-template location="templates/invoice-template.html"/>

<pdf:html-to-pdf outputPdfFileName="invoice.pdf"/>

<file:write path="#[vars.outputPath ++ '/' ++ attributes.fileName]"/>
```

This example converts an HTML template to a PDF document.

## Output Attributes

All operations return PDF attributes with the following properties:

- **fileName**: The name of the generated PDF file
- **pageCount**: The number of pages in the PDF
- **fileSize**: The size of the PDF file in bytes

## Technical Details

The module is built using:
- Apache PDFBox 3.0.5 for PDF manipulation
- OpenHTMLToPDF 1.1.28 for HTML to PDF conversion
- JSoup 1.20.1 for HTML parsing

## Requirements

- Mule Runtime 4.x
- Java 17 or later

## License

This project is licensed under the terms of the included LICENSE file.