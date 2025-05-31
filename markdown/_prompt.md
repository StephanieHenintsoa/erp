Analyze the provided CSV samples from three distinct .csv files, intended for import into an ERPNext application built on the Frappe Framework. Provide a concise explanation of the following:

Data Description: Briefly describe the type of data contained in each CSV file (e.g., fields, purpose, and entity represented).

Relationships: Identify and explain the relationships between the three CSV files (e.g., foreign key connections, shared fields, or dependencies).

Potential Import Errors: Highlight potential errors that may occur during import into ERPNext (e.g., missing mandatory fields, data type mismatches, duplicate entries, or invalid references).

Recommendations: Suggest best practices for importing these files into ERPNext, referencing the official ERPNext documentation (https://docs.frappe.io/erpnext/user/manual/en/introduction) and Frappe Framework documentation (https://docs.frappe.io/framework/user/en/introduction) where applicable.

CSV Data Samples:

    csv-1: 

    date,item_name,item_groupe,required_by,quantity,purpose,target_warehouse,ref
    02/05/2025,boulon,piece,02/06/2025,13,Purchase,All Warehouse,1
    02/05/2025,ciment,consommable,02/06/2025,5,Purchase,All Warehouse,2

    csv-2: 

    supplier_name,country,type
    Sanifer,Madagascar,Company
    Exxon,Usa,Company
    Electroplus,Madagascar,Company

    csv-3: 

    ref_request_quotation,supplier
    1,Sanifer
    1,Exxon
    2,Sanifer

Instructions:

Keep explanations brief and focused, avoiding unnecessary details unless requested.
If specific DocTypes in ERPNext are implied by the data, map the CSV fields to relevant ERPNext DocTypes (e.g., Item, Customer, Sales Order).

For relationships, clearly state which fields link the files (e.g., primary/foreign keys).
If any CSV data is incomplete or unclear, note assumptions made during analysis.

If no sample data is provided, request clarification or provide a generalized analysis based on typical ERPNext import scenarios.
Do not generate charts or images unless explicitly requested.

Additional Notes:

Ensure all references to ERPNext and Frappe Framework align with the official documentation.
If web searches or X posts are needed for context (e.g., common import issues), summarize findings briefly and cite sources.


/* =============================================================================================== */
/* =============================================================================================== */
/* =============================================================================================== */
/* =============================================================================================== */


Develop an **Import CSV functionality** for my Spring Boot application integrated with an ERPNext application (built on the Frappe Framework). The functionality should include a single form with three CSV file inputs. Follow these requirements:

1. **Import Logic**:
   - Implement a "tout-ou-rien" (all-or-nothing) principle: all rows across the three CSV files must be valid for any data to be inserted into the ERPNext database.
   - Validate all data before saving to the database, ensuring no errors exist.
   - Display a table below the form detailing any errors, with columns: `File | Line Number | Error Description`.

2. **Spring Boot Implementation**:
   - Adhere to the package structure: `java/com/example/erp/{config,controller,entity,service}`.
   - Keep controllers thin, handling only HTTP requests, validation, and redirection.
   - Place business logic (e.g., CSV parsing, validation, ERPNext API calls) in service classes.
   - Provide code for:
     - A controller to handle form submission and file uploads.
     - A service class to parse CSVs, validate data, and interact with ERPNext APIs.
     - Any necessary entities for temporary data storage or error tracking.
     - A configuration class if needed (e.g., for multipart file handling).

3. **ERPNext Implementation**:
   - Specify whether new DocTypes are required based on the CSV data (e.g., for custom entities or temporary error logging).
   - Identify if existing ERPNext DocTypes (e.g., Item, Customer) or files need updates.
   - Provide Frappe Framework code (e.g., Python scripts, custom API endpoints) if server-side logic is needed, referencing official documentation:
     - ERPNext: `https://docs.frappe.io/erpnext/user/manual/en/introduction`
     - Frappe Framework: `https://docs.frappe.io/framework/user/en/introduction`

4. **Error Handling**:
   - Validate CSV data against ERPNext DocType schemas (e.g., mandatory fields, data types).
   - Log errors for each file, including file name, line number, and detailed error description.
   - Return errors in a structured format for display in the UI table.

5. **Code Deliverables**:
   - For Spring Boot, include controller, service, and entity classes as separate artifacts if needed.
   - For ERPNext, include Python scripts, DocType JSON (if applicable), or modifications to existing files.
   - Ensure Spring Boot controllers follow best practices (thin, with business logic in services).
   - For the frontend, provide a simple HTML/JS form for file uploads and error table display, using modern JavaScript and Tailwind CSS.

6. **Assumptions and Clarifications**:
   - If CSV data structure is unknown, request sample data or assume typical ERPNext entities (e.g., Item, Customer, Sales Order).
   - Use ERPNext’s REST API for data import unless custom server-side logic is required.
   - Reference official ERPNext/Frappe documentation for import processes or API usage.
   - If web searches or X posts are needed for specific issues (e.g., common CSV import errors), summarize findings briefly.

**Instructions**:
- Keep responses concise, focusing on code and minimal explanations unless requested.
- Use UUIDs for new artifacts; reuse artifact IDs if updating previous ones.
- Ensure ERPNext code aligns with Frappe Framework best practices and documentation.
- For Spring Boot, follow the specified package structure and controller best practices.
- Avoid generating charts or images unless explicitly requested.


# CSV Import Functionality Requirements

## Spring Boot
- **Controller**: Handle form submission, file uploads; validate requests; redirect to service.
- **Service**: Parse CSVs, validate data, call ERPNext APIs, collect errors.
- **Entity**: Model for error tracking (e.g., `CsvError` with file, line, description).
- **Config**: Configure multipart file handling if needed.

## ERPNext
- **DocTypes**: Specify if new DocTypes are needed (e.g., for custom data or error logging).
- **Files**: Identify updates to existing files (e.g., custom scripts, API endpoints).
- **API**: Use ERPNext REST API for data import unless server-side logic is required.

## Frontend
- HTML/JS form with three file inputs and error table (columns: File, Line Number, Error Description).
- Use Tailwind CSS for styling; avoid `<form onSubmit>` due to sandbox restrictions.

## Validation
- Ensure all CSV rows are valid before database insertion ("tout-ou-rien").
- Validate against ERPNext DocType schemas (e.g., mandatory fields, data types).
- Display errors in a table with file name, line number, and description.



/* ================================================================= */
/* ================================================================= */
/* ================================================================= */
/* ================================================================= */

I want you to create a detailed, step-by-step **implementation guideline** for building a full CSV import functionality that transfers data from my **Spring Boot application** to my **ERPNext application**.

Please **do not include any code** in your response — only a well-structured **TODO list or implementation plan** with clear explanations.

---

### The guide should be divided into two main sections:

---

### 1. **In the Spring Boot Application**

* **Which files need to be created?**
  (e.g., Controller, Service, Entity, DTO, Configuration, etc.)

* **What specific configurations are required?**
  (e.g., file handling, multipart support, CSV parser setup, etc.)

* **What endpoints or workflows need to be implemented?**
  (e.g., Upload endpoint, validation logic, sending data to ERPNext)

* **How should the data be formatted or packaged before being sent to ERPNext?**
  (e.g., as JSON via REST API, or raw CSV via HTTP)

---

### 2. **In the ERPNext Application**

* **Which Python files (e.g., Doctype, API, or background jobs) should be created?**

* **Which JavaScript files (if any) need to be created or modified?**
  (e.g., for UI-based CSV import or client-side validation)

* **Where exactly should each file be placed?**
  (e.g., inside a specific app, module, or folder like `/doctype`, `/public/js`, etc.)

* **Do any existing files need to be updated or extended?**
  (If yes, specify which files and what kind of modifications)

* **Are any ERPNext hooks, events, or scheduled tasks required?**

* **Are any permissions or role configurations needed to allow this import?**

---

Please structure the guideline as a **chronological sequence** of actions, from start to finish, with each step being clear and unambiguous. Assume this is for a developer with working knowledge of both Spring Boot and ERPNext but not necessarily experience with integrating the two.
