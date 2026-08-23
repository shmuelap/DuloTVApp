// חסימת חלונות קופצים (Popups)
window.open = function() { return null; };

// מחיקת שכבות פרסומת שקופות שחוסמות לחיצות
setInterval(function() {
    let ads = document.querySelectorAll('iframe[src*="ads"], div[class*="ad"], div[id*="pop"]');
    ads.forEach(ad => ad.remove());
    
    // הגדלת נגן הווידאו למסך מלא במידה וקיים
    let video = document.querySelector('video');
    if (video) {
        video.style.width = '100vw';
        video.style.height = '100vh';
    }
}, 1000);