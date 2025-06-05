Analyze the provided CSV samples from three distinct .csv files, intended for import into an ERPNext with FRappe HR module application built on the Frappe Framework. Provide a concise explanation of the following:

The main theme focusing the 3 csv files is : Human Resource (employee and their salaries) 

Data Description: Briefly describe the type of data contained in each CSV file (e.g., fields, purpose, and entity represented).

Relationships: Identify and explain the relationships between the three CSV files (e.g., foreign key connections, shared fields, or dependencies).

Potential Import Errors: Highlight potential errors that may occur during import into ERPNext (e.g., missing mandatory fields, data type mismatches, duplicate entries, or invalid references).

Recommendations: Suggest best practices for importing these files into ERPNext, referencing the official ERPNext documentation (https://docs.frappe.io/erpnext/user/manual/en/introduction) and Frappe Framework documentation (https://docs.frappe.io/framework/user/en/introduction) and Frappe HR docs : (https://docs.frappe.io/hr/introduction)
where applicable.

CSV Data Samples:

    csv-1: 

    Ref,Nom,Prenom,genre,Date embauche,date naissance,company
    1,Rakoto,Alain,Masculin,03/04/2024,01/01/1980,My Company
    2,Rasoa,Jeanne,Feminin,08/06/2024,01/01/1990,My Company

    csv-2: 

    salary structure,name,Abbr,type,valeur,Remarque
    gasy1,Salaire Base,SB,earning,100%,
    gasy1,Indemnité,IND,earning,30%,salaire base
    gasy1,Taxe sociale,TS,deduction,20%,salaire base + indemnité

    csv-3: 

    Mois,Ref Employe,Salaire Base,Salaire
    01/04/2025,1,1500000,gasy1
    01/04/2025,2,900000,gasy1
    01/03/2025,1,1600000,gasy1
    01/03/2025,2,900000,gasy1

Instructions:

Keep explanations brief and focused, avoiding unnecessary details unless requested.
If specific DocTypes in ERPNext are implied by the data, map the CSV fields to relevant ERPNext DocTypes (e.g., Item, Customer, Sales Order).

For relationships, clearly state which fields link the files (e.g., primary/foreign keys).
If any CSV data is incomplete or unclear, note assumptions made during analysis.

If no sample data is provided, request clarification or provide a generalized analysis based on typical ERPNext import scenarios.
Do not generate charts or images unless explicitly requested.

Additional Notes:

Ensure all references to ERPNext, Frappe HR and Frappe Framework align with the official documentation.
If web searches or X posts are needed for context (e.g., common import issues), summarize findings briefly and cite sources.

/* =============================================================================================== */
/* =============================================================================================== */
/* =============================================================================================== */
/* =============================================================================================== */


I've read your analyze and here are my some returns: 

The CSV files provided to me are samples, but the headers will match those in the real data. However, the actual data may include additional rows, and the specific values (e.g., company names, salary components, salary structures, or employee naming conventions) are unknown and may differ from the samples. Below is the context and requirements for this task:

### Context
- **Sample CSV Files**: I have been provided with three sample CSV files to understand the CSV headers and structure.
- **Real Data**: On the presentation day, I will receive the final versions of the three CSV files containing the real data. I will not be able to modify the code or the application after receiving these files.
- **Import Process**: The Spring Boot application will:
  1. Import the data from the three CSV files into the ERPNext system.
  2. Validate the imported data to ensure accuracy (e.g., verify calculations and confirm successful import).
  3. Handle any errors that occur during the import process.
- **Dynamic Data Creation**: If any mandatory values are missing in the CSV files (e.g., required fields for companies, salary components, or salary structures), the import functionality must dynamically create these values during the import process. Pre-import setup is not feasible due to the variability in the real data.

### Requirements
To ensure a robust and successful import process on the presentation day, please provide detailed guidance on the following:

1. **Precautions and Best Practices**:
   - What measures should I take to ensure the import functionality works seamlessly with the real CSV files, given the potential variability in data?
   - How can I handle missing mandatory values dynamically during the import process? Provide specific strategies or techniques.
   - What validation checks should I implement to verify the accuracy of calculations and ensure all data is imported correctly?
   - How should I handle and report errors during the import process to facilitate debugging and ensure a smooth user experience?

2. **Potential Risks and Mitigation**:
   - What are the potential risks or challenges I might face when importing the real CSV files (e.g., inconsistent data formats, missing headers, or unexpected values)?
   - How can I mitigate these risks to ensure the import process is robust and reliable?



/* =============================================================================================== */
/* =============================================================================================== */
/* =============================================================================================== */
/* =============================================================================================== */

Here are the implementation instructions and take into considerations all of the things that we've been talking before. Don't add anything else apart the things mentionned here, unless it's really rquired but i've forget to mention it here, tell me specifically about this if it happens: 


1. **Import Logic**:
   - Implement a "tout-ou-rien" (all-or-nothing) principle: all rows across the three CSV files must be valid for any data to be inserted into the ERPNext database.
   - Validate all data before saving to the database, ensuring no errors exist.
   - Display a table below the form detailing any errors, with columns: `File | Line Number | Error Description`.
  ### Dynamic Data Handling Strategy

  **1. Implement Hierarchical Import Logic**
  1. Pre-validate all CSV structures and relationships
  2. Create missing master data dynamically (Company, Departments, etc.)
  3. Import Employees with auto-generated naming series
  4. Create Salary Components and Structures on-the-fly
  5. Import Salary Slips with validation

  **2. Dynamic Master Data Creation**
  - **Company Creation**: If company doesn't exist, create with minimal required fields
  - **Employee Naming**: Implement auto-generation (EMP-YYYY-##### format)
  - **Salary Components**: Auto-create components from CSV-2 with proper account mappings
  - **Department Assignment**: Create default department if missing

  **3. Robust Validation Framework**
  - Schema Validation: Check CSV headers and basic structure
  - Data Type Validation: Convert and validate dates, numbers, percentages
  - Business Logic Validation: Verify salary calculations match structure
  - Reference Integrity: Ensure all foreign key relationships exist
  - ERPNext API Validation: Confirm successful creation in ERPNext

  ### Critical Validation Checks

  **1. Salary Calculation Verification**
  - Recalculate salary components based on structure percentages
  - Compare calculated values with imported base salary amounts
  - Validate earning vs deduction classifications
  - Ensure monthly totals match expected calculations

  **2. Data Consistency Checks**
  - Verify employee-salary record relationships
  - Check for duplicate entries across months
  - Validate date ranges and chronological order
  - Confirm currency formatting and precision (EUR)

  ### High-Risk Scenarios

  **1. Data Format Variations**
  Risk: Date formats, number formats, encoding issues
  Mitigation:
  - Implement multiple date format parsers (DD/MM/YYYY, MM/DD/YYYY, etc.)
  - Handle various CSV encodings (UTF-8, ISO-8859-1, etc.)

  **2. Missing Critical Data**
  Risk: Empty mandatory fields, incomplete salary structures
  Mitigation:
  - Create comprehensive default value strategies
  - Build fallback data generation logic

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
     - Frappe HR: `https://docs.frappe.io/hr/introduction`
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
- **Thymeleaf**: A thymeleaf view with the single form within 3 inputs fields

## ERPNext
- **DocTypes**: Specify if new DocTypes are needed (e.g., for custom data or error logging).
- **Files**: Identify updates to existing files (e.g., custom scripts, API endpoints).
- **API**: Use ERPNext REST API for data import unless server-side logic is required.

## Frontend
- HTML/JS form with three file inputs and error table (columns: File, Line Number, Error Description).

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

* **What are the purpose of the file?**
  (e.g., This controller manages HTTP request to some ERP Next Endpoint, etc.)

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


/*--------------------*/