import { isMobile } from "../Hooks/MobileCheck";

class ToggleButtonControl {
  constructor( text, onclick ) {
    this.text = text;
    this._onclick = onclick;
  }

  onAdd() {
    this.container = document.createElement("div");
    this.container.className = "toggle-control"; 
    
    Object.assign(this.container.style, {
      pointerEvents: "auto",
      alignItems: "center",
      display: "flex",
      padding: "8px 12px 8px 8px",
      fontSize: "14px",
      cursor: "pointer", 
      width: "100%",
      alignItems: "center",
      justifyContent: "space-between",
    });

    this.label = document.createElement("span");
    this.label.textContent = this.text;
    this.label.style.fontWeight = "bold"; 

    this.checkbox = document.createElement("input");
    this.checkbox.type = "checkbox";
    this.checkbox.checked = true;
    this.checkbox.className = "toggle-checkbox"; 
    this.checkbox.style.pointerEvents = "none";
    this.checkbox.style.minWidth = "40px";
    this.checkbox.style.height = "40px";
    this.checkbox.style.borderRadius = "16px"; 
    this.checkbox.style.appearance = "none"; // Remove default browser styling
    this.checkbox.style.webkitAppearance = "none"; // Remove default browser styling for Safari/Chrome
    this.checkbox.style.mozAppearance = "none"; // Remove default browser styling for Firefox
    this.checkbox.style.border = "1px solid #ccc"; // Add a border to see the rounding
    this.checkbox.style.marginRight = "6px"; 
    this.checkbox.style.border = "1px solid #ccc";

    this.container.appendChild(this.label);
    this.container.appendChild(this.checkbox);

    this.container.addEventListener("click", this._onclick);

    return this.container;
  }

  onRemove() {
    if (this.container && this.container.parentNode) {
      this.container.parentNode.removeChild(this.container);
    }
  }

  // // Method to externally update the checked state if needed
  // setChecked(isChecked) {
  //   this.isChecked = isChecked;
  //   if (this.checkbox) {
  //     this.checkbox.checked = isChecked;
  //   }
  // }
}

export default ToggleButtonControl;