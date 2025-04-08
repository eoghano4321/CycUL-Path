class ToggleButtonControl {
  constructor( text, onclick ) {
    this.text = text;
    this._onclick = onclick;
  }

  onAdd() {
    this.button = document.createElement("button");
    this.button.innerHTML = this.text;
    
    this.button.style.pointerEvents = "auto";
    Object.assign(this.button.style, {
      background: "rgba(19, 54, 110, 0.8)",
      color: "#FFF",
      padding: "8px 12px",
      fontSize: "14px",
      borderRadius: "16px",
      cursor: "pointer", 
      boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
      transition: "background 0.2s ease-in-out",
    });
    this.button.addEventListener("click", this._onclick);
    return this.button;
  }
  

  onRemove() {
    this.button.parentNode.removeChild(this.button);
  }
}

export default ToggleButtonControl;