document.querySelectorAll(".hover-card").forEach((card) => {
    const gloss = card.querySelector(".gloss");
    card.addEventListener("mousemove", (event) => {
        const pointX = event.clientX;
        const pointY = event.clientY;

        const cardRect = card.getBoundingClientRect();

        const halfHeight = card.offsetHeight / 2;
        const halfWidth = card.offsetWidth / 2;

        const cardCenterX = cardRect.left + halfWidth;
        const carcCenterY = cardRect.top + halfHeight;

        const deltaX = pointX - cardCenterX;
        const deltaY = pointY - carcCenterY;

        const rx = deltaY / halfHeight;
        const ry = deltaX / halfWidth;

        const distaceToCenter = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
        const maxDist = Math.max(halfHeight, halfWidth);
        const deg = distaceToCenter * 10 / maxDist;

        card.style.transform = `perspective(400px) rotate3D(${-rx}, ${ry}, 0, ${deg}deg)`;
        gloss.style.transform = `translate(${-ry * 100}%, ${-rx * 100}%) scale(2.4)`;
        gloss.style.opacity = distaceToCenter * 0.05 / maxDist;

    });

    card.addEventListener("mouseleave", () => {
        card.style = null;
        gloss.style.opacity = 0;
    });
});