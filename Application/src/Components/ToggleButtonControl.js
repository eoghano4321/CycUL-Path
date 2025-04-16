import { isMobile } from "../Hooks/MobileCheck";

class ToggleButtonControl {
  constructor( text, icon, onclick ) {
    this.text = text;
    this.icon = icon;
    this._onclick = onclick;
  }

  onAdd() {
    this.button = document.createElement("button");
    this.button.className = "toggle-button";
    if(isMobile()){
      this.button.innerHTML = `<img src="${this.icon}" style="width: 16px; height: 16px; scale: 1.5">`;
      
      Object.assign(this.button.style, {
        pointerEvents: "auto",
        background: "rgba(19, 54, 110, 0.8)",
        alignItems: "center",
        display: "flex",
        color: "#FFF",
        padding: "8px",
        borderRadius: "16px",
        cursor: "pointer", 
        boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
        transition: "background 0.2s ease-in-out",
      });
      this.button.title = this.text;
      
    } else{
      this.button.innerHTML = this.text;
      
      Object.assign(this.button.style, {
        pointerEvents: "auto",
        background: "rgba(19, 54, 110, 0.8)",
        color: "#FFF",
        padding: "8px 12px",
        fontSize: "14px",
        borderRadius: "16px",
        cursor: "pointer", 
        boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
        transition: "background 0.2s ease-in-out",
      });
    }
    this.button.addEventListener("click", this._onclick);
    return this.button;
  }
  

  onRemove() {
    this.button.parentNode.removeChild(this.button);
  }
}

export default ToggleButtonControl;