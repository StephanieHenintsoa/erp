from frappe import _
import frappe
from frappe.utils import nowdate

@frappe.whitelist()
def create_salary_structure_assignment(employee, salary_structure, from_date, company, base, variable=None):
    """
    Crée un nouvel assignment de structure salariale
    
    Args:
        employee (str): ID de l'employé
        salary_structure (str): Nom de la structure salariale
        from_date (str): Date de début au format YYYY-MM-DD
        company (str): Nom de la société
        base (float): Salaire de base
        variable (float, optional): Partie variable du salaire. Defaults to None.
    
    Returns:
        dict: Statut de la création et détails de l'assignment
    """
    
    # Validation des paramètres obligatoires
    if not all([employee, salary_structure, from_date, company, base]):
        frappe.throw(_("Employee, Salary Structure, From Date, Company and Base are mandatory fields"))
    
    try:
        # Création d'un nouveau document Salary Structure Assignment
        ssa = frappe.new_doc("Salary Structure Assignment")
        ssa.employee = employee
        ssa.salary_structure = salary_structure
        ssa.from_date = from_date
        ssa.company = company
        ssa.base = base
        
        if variable:
            ssa.variable = variable
        
        # Définition du nom du document
        ssa.name = f"{employee} - {salary_structure} - {frappe.utils.format_date(from_date)}"
        
        # Sauvegarde du document
        ssa.insert(ignore_permissions=True)
        
        return {
            "status": "success",
            "message": _("Salary Structure Assignment created successfully"),
            "name": ssa.name
        }
        
    except Exception as e:
        frappe.log_error(frappe.get_traceback(), _("Salary Structure Assignment creation failed"))
        return {
            "status": "error",
            "message": _("Failed to create Salary Structure Assignment: {0}").format(str(e))
        }