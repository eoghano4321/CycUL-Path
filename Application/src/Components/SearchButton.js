import Logo from "../Assets/Logo.svg";

class SearchButton {
    constructor( text ) {
      this.text = text;
    }
  
    onAdd() {
      this.button = document.createElement("button");
      
      this.button.style.pointerEvents = "auto";
      Object.assign(this.button.style, {
        background: "rgba(19, 54, 110, 0.8)",
        color: "#FFF",
        padding: "8px 12px",
        fontSize: "14px",
        borderRadius: "16px",
        display: "inline-flex",
        cursor: "pointer", 
        boxShadow: "0 2px 4px rgba(0,0,0,0.2)",
        minWidth: "240px",
        alignItems: "center",
        justifyContent: "center",
      });
      const logoElement = document.createElement("img");
      logoElement.src = Logo;
      logoElement.style.width = "16px";
      logoElement.style.height = "16px";
      logoElement.style.marginLeft = "16px";
      logoElement.style.marginRight = "8px";

      // Create text node
      const textNode = document.createElement("text");
      textNode.innerHTML = this.text;
      textNode.style.marginLeft = "8px";

      // Append logo and text to button
      this.button.appendChild(textNode);
      this.button.appendChild(logoElement);
      
      this.button.addEventListener("click", () => {
        console.log("Button clicked");
        console.log(this._onclickhandler);
        if (this._onclickhandler) {
          this._onclickhandler();
        }
      }
      );
      return this.button;
    }

    updateOnClick(searchFunction) {
      console.log("Updating button click handler");
      if (this.button) {
        this._onclickhandler = searchFunction;
      }
    }
  
    onRemove() {
      this.button.parentNode.removeChild(this.button);
    }
  }
  
  export default SearchButton;