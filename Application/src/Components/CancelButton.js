export default function CancelButton(onClick) {
    const button = document.createElement('button');
    button.textContent = '⨉';
    button.className = 'cancel-button__btn';
    button.style.background = 'rgba(19, 54, 110, 0.8)';
    button.style.color = '#FFF';
    button.style.padding = '8px 12px';
    button.style.fontSize = '14px';
    button.style.borderRadius = '16px';
    button.style.cursor = 'pointer';
    button.style.boxShadow = '0 2px 4px rgba(0,0,0,0.2)';
    button.style.transition = 'background 0.2s ease-in-out';
    button.style.minWidth = '40px';

    button.addEventListener('click', onClick);

    return button;
}