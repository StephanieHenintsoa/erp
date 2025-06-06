
GET http://erpnext.localhost:8000/api/resource/Salary Slip?fields=["*"]

{
    "data": [
        {
            "name": "Sal Slip/HR-EMP-00005/00001",
            "owner": "Administrator",
            "creation": "2025-06-01 20:52:03.298117",
            "modified": "2025-06-01 20:52:14.849999",
            "modified_by": "Administrator",
            "docstatus": 1,
            "idx": 2,
            "employee": "HR-EMP-00005",
            "employee_name": "Mariah",
            "company": "ITU (Demo)",
            "department": "Management - ITUD",
            "designation": "Analyst",
            "branch": null,
            "posting_date": "2025-06-01",
            "letter_head": null,
            "status": "Submitted",
            "salary_withholding": null,
            "salary_withholding_cycle": null,
            "currency": "EUR",
            "exchange_rate": 1.0,
            "payroll_frequency": "Monthly",
            "start_date": "2025-06-01",
            "end_date": "2025-06-30",
            "salary_structure": "S_mah",
            "payroll_entry": null,
            "mode_of_payment": "",
            "salary_slip_based_on_timesheet": 0,
            "deduct_tax_for_unclaimed_employee_benefits": 0,
            "deduct_tax_for_unsubmitted_tax_exemption_proof": 0,
            "total_working_days": 30.0,
            "unmarked_days": 0.0,
            "leave_without_pay": 0.0,
            "absent_days": 0.0,
            "payment_days": 30.0,
            "total_working_hours": 0.0,
            "hour_rate": 0.0,
            "base_hour_rate": 0.0,
            "gross_pay": 500.0,
            "base_gross_pay": 500.0,
            "gross_year_to_date": 500.0,
            "base_gross_year_to_date": 0.0,
            "total_deduction": 50.0,
            "base_total_deduction": 50.0,
            "net_pay": 450.0,
            "base_net_pay": 450.0,
            "rounded_total": 450.0,
            "base_rounded_total": 450.0,
            "year_to_date": 450.0,
            "base_year_to_date": 0.0,
            "month_to_date": 450.0,
            "base_month_to_date": 0.0,
            "total_in_words": "EUR Four Hundred And Fifty only.",
            "base_total_in_words": "EUR Four Hundred And Fifty only.",
            "ctc": 0.0,
            "income_from_other_sources": 0.0,
            "total_earnings": 0.0,
            "non_taxable_earnings": 0.0,
            "standard_tax_exemption_amount": 0.0,
            "tax_exemption_declaration": 0.0,
            "deductions_before_tax_calculation": 0.0,
            "annual_taxable_amount": 0.0,
            "income_tax_deducted_till_date": 0.0,
            "current_month_income_tax": 0.0,
            "future_income_tax_deductions": 0.0,
            "total_income_tax": 0.0,
            "journal_entry": null,
            "amended_from": null,
            "bank_name": null,
            "bank_account_no": null
        },
        ...
    ]
}

required fields from this JSON data : 
- name
- employee
- employee_name
- gross pay
- net_pay


---
---
---

GET http://erpnext.localhost:8000/api/resource/Salary Slip/Sal Slip/HR-EMP-00005/00001?fields=["*"]

{
    "data": {
        "name": "Sal Slip/HR-EMP-00005/00001",
        "owner": "Administrator",
        "creation": "2025-06-01 20:52:03.298117",
        "modified": "2025-06-01 20:52:14.849999",
        "modified_by": "Administrator",
        "docstatus": 1,
        "idx": 2,
        "employee": "HR-EMP-00005",
        "employee_name": "Mariah",
        "company": "ITU (Demo)",
        "department": "Management - ITUD",
        "designation": "Analyst",
        "posting_date": "2025-06-01",
        "status": "Submitted",
        "currency": "EUR",
        "exchange_rate": 1.0,
        "payroll_frequency": "Monthly",
        "start_date": "2025-06-01",
        "end_date": "2025-06-30",
        "salary_structure": "S_mah",
        "mode_of_payment": "",
        "salary_slip_based_on_timesheet": 0,
        "deduct_tax_for_unclaimed_employee_benefits": 0,
        "deduct_tax_for_unsubmitted_tax_exemption_proof": 0,
        "total_working_days": 30.0,
        "unmarked_days": 0.0,
        "leave_without_pay": 0.0,
        "absent_days": 0.0,
        "payment_days": 30.0,
        "total_working_hours": 0.0,
        "hour_rate": 0.0,
        "base_hour_rate": 0.0,
        "gross_pay": 500.0,
        "base_gross_pay": 500.0,
        "gross_year_to_date": 500.0,
        "base_gross_year_to_date": 0.0,
        "total_deduction": 50.0,
        "base_total_deduction": 50.0,
        "net_pay": 450.0,
        "base_net_pay": 450.0,
        "rounded_total": 450.0,
        "base_rounded_total": 450.0,
        "year_to_date": 450.0,
        "base_year_to_date": 0.0,
        "month_to_date": 450.0,
        "base_month_to_date": 0.0,
        "total_in_words": "EUR Four Hundred And Fifty only.",
        "base_total_in_words": "EUR Four Hundred And Fifty only.",
        "ctc": 0.0,
        "income_from_other_sources": 0.0,
        "total_earnings": 0.0,
        "non_taxable_earnings": 0.0,
        "standard_tax_exemption_amount": 0.0,
        "tax_exemption_declaration": 0.0,
        "deductions_before_tax_calculation": 0.0,
        "annual_taxable_amount": 0.0,
        "income_tax_deducted_till_date": 0.0,
        "current_month_income_tax": 0.0,
        "future_income_tax_deductions": 0.0,
        "total_income_tax": 0.0,
        "doctype": "Salary Slip",
        "timesheets": [],
        "earnings": [
            {
                "name": "g53quk5sbt",
                "owner": "Administrator",
                "creation": "2025-06-01 20:52:03.298117",
                "modified": "2025-06-01 20:52:14.849999",
                "modified_by": "Administrator",
                "docstatus": 1,
                "idx": 1,
                "salary_component": "S_mah",
                "abbr": "smah",
                "amount": 500.0,
                "year_to_date": 500.0,
                "is_recurring_additional_salary": 0,
                "statistical_component": 0,
                "depends_on_payment_days": 1,
                "exempted_from_income_tax": 0,
                "is_tax_applicable": 1,
                "is_flexible_benefit": 0,
                "variable_based_on_taxable_salary": 0,
                "do_not_include_in_total": 0,
                "deduct_full_tax_on_selected_payroll_date": 0,
                "amount_based_on_formula": 0,
                "default_amount": 500.0,
                "additional_amount": 0.0,
                "tax_on_flexible_benefit": 0.0,
                "tax_on_additional_salary": 0.0,
                "parent": "Sal Slip/HR-EMP-00005/00001",
                "parentfield": "earnings",
                "parenttype": "Salary Slip",
                "doctype": "Salary Detail"
            }
        ],
        "deductions": [
            {
                "name": "g53oh2cejp",
                "owner": "Administrator",
                "creation": "2025-06-01 20:52:03.298117",
                "modified": "2025-06-01 20:52:14.849999",
                "modified_by": "Administrator",
                "docstatus": 1,
                "idx": 1,
                "salary_component": "Income Tax",
                "abbr": "IT",
                "amount": 50.0,
                "year_to_date": 50.0,
                "is_recurring_additional_salary": 0,
                "statistical_component": 0,
                "depends_on_payment_days": 1,
                "exempted_from_income_tax": 0,
                "is_tax_applicable": 1,
                "is_flexible_benefit": 0,
                "variable_based_on_taxable_salary": 0,
                "do_not_include_in_total": 0,
                "deduct_full_tax_on_selected_payroll_date": 0,
                "amount_based_on_formula": 0,
                "default_amount": 50.0,
                "additional_amount": 0.0,
                "tax_on_flexible_benefit": 0.0,
                "tax_on_additional_salary": 0.0,
                "parent": "Sal Slip/HR-EMP-00005/00001",
                "parentfield": "deductions",
                "parenttype": "Salary Slip",
                "doctype": "Salary Detail"
            }
        ],
        "leave_details": []
    }
}

required fields from this JSON data : 
- earnings 
    - name
    - amount
    - salary_component
    - abbr

- deductions
    - name
    - amount
    - salary_component
    - abbr

---
---
---


the goal is to get the salary slips details of a salary slips
use those api endpoint, retrieve only the required mentionned fields and create the appropriate DTO class and service class to map it correctly


