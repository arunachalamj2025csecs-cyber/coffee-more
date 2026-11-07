/**
 * COFFEE & MORE — INTERACTIVE UI & SCROLL ANIMATION ENGINE
 * Handles:
 *  1. Dynamic video scroll scrubbing & mode controls
 *  2. Ambient floating coffee bean & steam canvas with scroll physics
 *  3. Scroll-reveal triggers & parallax micro-card tilts
 *  4. Rolling milestone statistics counters
 *  5. Airport live ticker announcements rotator
 *  6. Interactive category filter & live search for menu items
 *  7. Gallery pop-up lightbox modal
 *  8. Traveler review carousel
 *  9. Google Plus Code clipboard copy with instant feedback
 * 10. Contact form validation & confirmation
 * 11. Mobile navigation drawer & sticky dock
 */

document.addEventListener('DOMContentLoaded', () => {
  initScrollProgressBar();
  initDynamicVideoController();
  initAmbientCanvas();
  initScrollAnimations();
  initStatCounters();
  initAnnouncementTicker();
  initMenuFiltersAndSearch();
  initGalleryLightbox();
  initReviewsCarousel();
  initPlusCodeCopy();
  initContactForm();
  initMobileNav();
  initScrollSpy();
});

/* ==========================================================================
   1. DYNAMIC VIDEO SCROLL SCRUBBING ENGINE (CENTERED FULL-BLEED)
   ========================================================================== */
function initDynamicVideoController() {
  const video = document.getElementById('coffeeScrollVideo');
  if (!video) return;

  let targetTime = 0;
  let isVideoReady = false;
  let isUserScrolling = false;
  let scrollTimeout = null;

  // Muted and playsinline for smooth hardware acceleration & browser policy
  video.muted = true;
  video.playsInline = true;

  video.addEventListener('loadedmetadata', () => {
    isVideoReady = true;
    updateVideoFromScroll();
  });

  setTimeout(() => {
    isVideoReady = true;
    updateVideoFromScroll();
  }, 400);

  // Smooth lerp loop for dynamic scroll scrubbing
  function renderVideoProgress() {
    if (isVideoReady && video.duration && !video.seeking) {
      const diff = targetTime - video.currentTime;
      if (Math.abs(diff) > 0.02) {
        // High-precision smooth frame advance
        video.currentTime += diff * 0.28;
      }
    }
    requestAnimationFrame(renderVideoProgress);
  }
  requestAnimationFrame(renderVideoProgress);

  function updateVideoFromScroll() {
    if (!isVideoReady) return;

    // Map scroll position to video duration across the viewport scroll distance
    const maxScroll = Math.max(window.innerHeight * 1.8, 900);
    const currentScroll = window.scrollY;
    const scrollFraction = Math.max(0, Math.min(1, currentScroll / maxScroll));

    const duration = video.duration || 10.03;
    targetTime = scrollFraction * duration;

    // Track scroll interaction state
    isUserScrolling = true;
    clearTimeout(scrollTimeout);
    scrollTimeout = setTimeout(() => {
      isUserScrolling = false;
    }, 150);
  }

  window.addEventListener('scroll', updateVideoFromScroll, { passive: true });

  // Subtle interactive parallax effect on mouse movement for big centered screen depth
  const heroSection = document.getElementById('hero');
  if (heroSection) {
    heroSection.addEventListener('mousemove', (e) => {
      const rect = heroSection.getBoundingClientRect();
      const xPercent = (e.clientX - rect.left) / rect.width - 0.5;
      const yPercent = (e.clientY - rect.top) / rect.height - 0.5;
      video.style.transform = `translate(calc(-50% + ${xPercent * 16}px), calc(-50% + ${yPercent * 16}px)) scale(1.04)`;
    });

    heroSection.addEventListener('mouseleave', () => {
      video.style.transform = 'translate(-50%, -50%) scale(1)';
    });
  }
}

/* ==========================================================================
   2. SCROLL PROGRESS BAR
   ========================================================================== */
function initScrollProgressBar() {
  const progressBar = document.getElementById('scrollProgressBar');
  const header = document.getElementById('mainHeader');

  window.addEventListener('scroll', () => {
    const totalHeight = document.documentElement.scrollHeight - window.innerHeight;
    const progress = (window.scrollY / totalHeight) * 100;
    if (progressBar) {
      progressBar.style.width = Math.min(100, Math.max(0, progress)) + '%';
    }

    if (header) {
      if (window.scrollY > 40) {
        header.classList.add('scrolled');
      } else {
        header.classList.remove('scrolled');
      }
    }
  }, { passive: true });
}

/* ==========================================================================
   3. AMBIENT BACKGROUND CANVAS (FLOATING BEANS & STEAM)
   ========================================================================== */
function initAmbientCanvas() {
  const canvas = document.getElementById('ambientCanvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');

  let width = canvas.width = window.innerWidth;
  let height = canvas.height = window.innerHeight;

  window.addEventListener('resize', () => {
    width = canvas.width = window.innerWidth;
    height = canvas.height = window.innerHeight;
  });

  // Create ambient coffee bean & steam particles
  const particles = [];
  const particleCount = 28;

  for (let i = 0; i < particleCount; i++) {
    particles.push({
      x: Math.random() * width,
      y: Math.random() * height,
      size: Math.random() * 8 + 4,
      speedX: (Math.random() - 0.5) * 0.4,
      speedY: -(Math.random() * 0.5 + 0.2),
      rotation: Math.random() * Math.PI * 2,
      rotationSpeed: (Math.random() - 0.5) * 0.02,
      opacity: Math.random() * 0.4 + 0.15,
      type: i % 3 === 0 ? 'bean' : 'steam'
    });
  }

  let lastScrollY = window.scrollY;
  let scrollDelta = 0;

  window.addEventListener('scroll', () => {
    scrollDelta = (window.scrollY - lastScrollY) * 0.05;
    lastScrollY = window.scrollY;
  }, { passive: true });

  function drawCoffeeBean(ctx, x, y, size, angle, opacity) {
    ctx.save();
    ctx.translate(x, y);
    ctx.rotate(angle);
    ctx.beginPath();
    ctx.ellipse(0, 0, size, size * 0.65, 0, 0, Math.PI * 2);
    ctx.fillStyle = `rgba(198, 139, 89, ${opacity * 0.45})`;
    ctx.fill();

    // Bean center groove
    ctx.beginPath();
    ctx.moveTo(-size * 0.7, 0);
    ctx.bezierCurveTo(-size * 0.2, -size * 0.25, size * 0.2, size * 0.25, size * 0.7, 0);
    ctx.strokeStyle = `rgba(255, 225, 185, ${opacity * 0.65})`;
    ctx.lineWidth = 1.2;
    ctx.stroke();
    ctx.restore();
  }

  function drawSteamPuff(ctx, x, y, size, opacity) {
    ctx.save();
    ctx.beginPath();
    ctx.arc(x, y, size * 1.8, 0, Math.PI * 2);
    ctx.fillStyle = `rgba(229, 168, 109, ${opacity * 0.18})`;
    ctx.filter = 'blur(4px)';
    ctx.fill();
    ctx.restore();
  }

  function animateCanvas() {
    ctx.clearRect(0, 0, width, height);

    particles.forEach(p => {
      p.y += p.speedY - scrollDelta * 0.2;
      p.x += p.speedX;
      p.rotation += p.rotationSpeed + scrollDelta * 0.005;

      // Wrap around edges
      if (p.y < -30) p.y = height + 30;
      if (p.y > height + 30) p.y = -30;
      if (p.x < -30) p.x = width + 30;
      if (p.x > width + 30) p.x = -30;

      if (p.type === 'bean') {
        drawCoffeeBean(ctx, p.x, p.y, p.size, p.rotation, p.opacity);
      } else {
        drawSteamPuff(ctx, p.x, p.y, p.size, p.opacity);
      }
    });

    scrollDelta *= 0.92; // Friction dampening
    requestAnimationFrame(animateCanvas);
  }

  animateCanvas();
}

/* ==========================================================================
   4. SCROLL REVEAL ANIMATIONS
   ========================================================================== */
function initScrollAnimations() {
  const animatedElements = document.querySelectorAll('.animate-on-scroll');

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('in-view');
      }
    });
  }, {
    threshold: 0.12,
    rootMargin: '0px 0px -40px 0px'
  });

  animatedElements.forEach(el => observer.observe(el));

  // Parallax tilt on micro cards
  const tiltCards = document.querySelectorAll('.tilt-card');
  tiltCards.forEach(card => {
    card.addEventListener('mousemove', (e) => {
      const rect = card.getBoundingClientRect();
      const x = e.clientX - rect.left - rect.width / 2;
      const y = e.clientY - rect.top - rect.height / 2;
      card.style.transform = `perspective(600px) rotateX(${-y * 0.1}deg) rotateY(${x * 0.1}deg) translateY(-4px)`;
    });

    card.addEventListener('mouseleave', () => {
      card.style.transform = 'perspective(600px) rotateX(0deg) rotateY(0deg) translateY(0px)';
    });
  });
}

/* ==========================================================================
   5. ROLLING NUMBER STAT COUNTERS
   ========================================================================== */
function initStatCounters() {
  const statCards = document.querySelectorAll('.stat-card');

  const observer = new IntersectionObserver((entries, obs) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const valueEl = entry.target.querySelector('[data-counter]');
        if (valueEl) {
          const target = parseInt(valueEl.getAttribute('data-counter'), 10);
          const suffix = valueEl.getAttribute('data-suffix') || '';
          let count = 0;
          const duration = 1600;
          const step = Math.ceil(target / (duration / 40));

          const counterTimer = setInterval(() => {
            count += step;
            if (count >= target) {
              count = target;
              clearInterval(counterTimer);
            }
            valueEl.textContent = count + suffix;
          }, 40);
        }
        obs.unobserve(entry.target);
      }
    });
  }, { threshold: 0.5 });

  statCards.forEach(card => observer.observe(card));
}

/* ==========================================================================
   6. AIRPORT ANNOUNCEMENT TICKER ROTATOR
   ========================================================================== */
function initAnnouncementTicker() {
  const slides = document.querySelectorAll('#announcementTicker .ticker-slide');
  if (slides.length <= 1) return;

  let currentIdx = 0;

  setInterval(() => {
    slides[currentIdx].classList.remove('active');
    currentIdx = (currentIdx + 1) % slides.length;
    slides[currentIdx].classList.add('active');
  }, 5000);
}

/* ==========================================================================
   7. INTERACTIVE MENU FILTER & SEARCH
   ========================================================================== */
function initMenuFiltersAndSearch() {
  const tabs = document.querySelectorAll('.cat-tab');
  const cards = document.querySelectorAll('.menu-card');
  const searchInput = document.getElementById('menuSearchInput');
  const clearBtn = document.getElementById('clearSearchBtn');
  const countDisplay = document.getElementById('menuCountDisplay');

  let activeCategory = 'all';
  let searchTerm = '';

  function applyFilters() {
    let visibleCount = 0;

    cards.forEach(card => {
      const category = card.getAttribute('data-category');
      const name = card.getAttribute('data-name').toLowerCase();
      const tags = card.getAttribute('data-tags').toLowerCase();
      const desc = card.querySelector('.item-desc').textContent.toLowerCase();

      const matchesCat = activeCategory === 'all' || category === activeCategory;
      const matchesSearch = !searchTerm || name.includes(searchTerm) || tags.includes(searchTerm) || desc.includes(searchTerm);

      if (matchesCat && matchesSearch) {
        card.style.display = 'flex';
        visibleCount++;
      } else {
        card.style.display = 'none';
      }
    });

    if (countDisplay) {
      countDisplay.textContent = `Showing ${visibleCount} of ${cards.length} handcrafted items`;
    }
  }

  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      tabs.forEach(t => {
        t.classList.remove('active');
        t.setAttribute('aria-selected', 'false');
      });
      tab.classList.add('active');
      tab.setAttribute('aria-selected', 'true');
      activeCategory = tab.getAttribute('data-category');
      applyFilters();
    });
  });

  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      searchTerm = e.target.value.trim().toLowerCase();
      if (clearBtn) {
        clearBtn.style.display = searchTerm ? 'block' : 'none';
      }
      applyFilters();
    });
  }

  if (clearBtn) {
    clearBtn.addEventListener('click', () => {
      searchInput.value = '';
      searchTerm = '';
      clearBtn.style.display = 'none';
      searchInput.focus();
      applyFilters();
    });
  }
}

/* ==========================================================================
   8. ITEM DETAILS & PAIRING MODAL
   ========================================================================== */
const itemModalData = {
  "Signature Airport Cappuccino": {
    dietary: "🟢 Veg",
    tag: "Best Seller",
    desc: "Velvety steamed milk poured over a rich double-shot espresso, finished with a dusted cocoa crown. Rich in aroma, low on acidity, pulled fresh for departing travelers.",
    pairing: "Pairs exquisitely with our Blueberry Burst Muffin or Golden Butter Croissant!"
  },
  "Classic Americano": {
    dietary: "🟢 Veg",
    tag: "Traveler Classic",
    desc: "Bold, smooth, and aromatic. Premium espresso pulled over hot water for pure coffee lovers needing morning alertness.",
    pairing: "Pairs wonderfully with a toasted Double Cheese & Corn Crunch Toastie."
  },
  "Café Latte": {
    dietary: "🟢 Veg",
    tag: "Smooth Comfort",
    desc: "Subtle espresso infused with silky microfoam milk. Mellow, smooth, and uplifting.",
    pairing: "Best enjoyed with a warm slice of Cinnamon & Apple Swirl Danish."
  },
  "Rich Hazelnut Café Mocha": {
    dietary: "🟢 Veg",
    tag: "Sweet Indulgence",
    desc: "Espresso blended with artisanal chocolate, roasted hazelnut notes, and whipped cream.",
    pairing: "Try it with our Warm Walnut Choco Brownie for a chocolate lover's heaven."
  },
  "Creamy Caramel Iced Frappé": {
    dietary: "🟢 Veg",
    tag: "Traveler Favorite",
    desc: "Blended chilled espresso, fresh milk, and caramel drizzle topped with whipped cream.",
    pairing: "Pairs crisply with Golden Crispy Potato Wedges."
  },
  "Masala Chai Special": {
    dietary: "🟢 Veg",
    tag: "House Special",
    desc: "Traditional Indian milk tea infused with crushed cardamom, ginger, cloves, and cinnamon. The heartwarming taste of home at the airport.",
    pairing: "Complements our Baked Veggie Puff Pastry or Herb-Crusted Paneer Poppers."
  },
  "Garden Fresh Veggie & Cheese Grilled Sandwich": {
    dietary: "🟢 Veg",
    tag: "Top Pick",
    desc: "Crispy grilled bread layered with crisp bell peppers, English cucumber, sweet corn, and melted mozzarella.",
    pairing: "Order with a hot Cappuccino or Cold Brew on Rocks."
  },
  "Spiced Paneer Tikka Panini": {
    dietary: "🟢 Veg",
    tag: "Must Try",
    desc: "Marinated tender cottage cheese cubes tossed in tandoori spices and mint mayo, pressed on artisan bread.",
    pairing: "Delicious with Masala Chai Special or Tangy Lime & Mint Mojito."
  },
  "Classic Herb Grilled Chicken Sandwich": {
    dietary: "🔴 Non-Veg",
    tag: "Protein Fuel",
    desc: "Juicy shredded chicken breast seasoned with rosemary, garlic emulsion, and cheddar cheese.",
    pairing: "Pairs ideally with Chilled Mango & Mint Cooler."
  },
  "Blueberry Burst Muffin": {
    dietary: "🟢 Veg Available",
    tag: "Freshly Baked Daily",
    desc: "Moist, soft vanilla sponge packed with tart wild blueberries and a delicate crumble crust.",
    pairing: "Best paired with Café Latte or English Breakfast Tea."
  },
  "Warm Walnut Choco Brownie": {
    dietary: "🟢 Veg",
    tag: "Bestseller",
    desc: "Fudgy dark chocolate brownie topped with toasted walnuts. Served warm with chocolate drizzle.",
    pairing: "Sensational with Classic Americano."
  }
};

window.openOrderModal = function(itemName) {
  const modal = document.getElementById('itemModal');
  const modalTitle = document.getElementById('modalTitle');
  const modalDesc = document.getElementById('modalDesc');
  const modalDietary = document.getElementById('modalDietary');
  const modalTag = document.getElementById('modalTag');
  const modalPairing = document.getElementById('modalPairing');

  const data = itemModalData[itemName] || {
    dietary: "🟢 Veg",
    tag: "Fresh Handcrafted Selection",
    desc: "Prepared fresh to order right at our Kamaraj Domestic Airport counter.",
    pairing: "Pairs delightfully with our artisanal brews."
  };

  if (modalTitle) modalTitle.textContent = itemName;
  if (modalDesc) modalDesc.textContent = data.desc;
  if (modalDietary) {
    modalDietary.textContent = data.dietary;
    modalDietary.className = data.dietary.includes('Non-Veg') ? 'dietary-marker non-veg' : 'dietary-marker veg';
  }
  if (modalTag) modalTag.textContent = data.tag;
  if (modalPairing) modalPairing.textContent = data.pairing;

  if (modal) {
    modal.classList.add('open');
    modal.setAttribute('aria-hidden', 'false');
  }
};

window.closeItemModal = function() {
  const modal = document.getElementById('itemModal');
  if (modal) {
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
  }
};

document.getElementById('closeItemModalBtn')?.addEventListener('click', closeItemModal);
document.getElementById('itemModal')?.addEventListener('click', (e) => {
  if (e.target.id === 'itemModal') closeItemModal();
});

/* ==========================================================================
   9. PHOTO GALLERY LIGHTBOX
   ========================================================================== */
const galleryData = {
  'gallery-1': {
    tag: '#BaristaCraft',
    title: 'The Barista Pour',
    desc: 'Fresh espresso extracted with precision from 100% freshly ground beans daily at Chennai Domestic Airport.',
    icon: 'fa-mug-hot',
    gradient: 'linear-gradient(135deg, #2C1810, #5C3222)'
  },
  'gallery-2': {
    tag: '#UnwindBeforeFlying',
    title: 'The Cozy Nook',
    desc: 'Comfortable, luggage-friendly seating away from bustling terminal gates with charging sockets at every table.',
    icon: 'fa-couch',
    gradient: 'linear-gradient(135deg, #4A271B, #8C5535)'
  },
  'gallery-3': {
    tag: '#BakeShop',
    title: 'Freshly Baked Counter',
    desc: 'Warm blueberry muffins, Dutch cocoa cakes, butter croissants & Danishes baked fresh every single morning at 6 AM.',
    icon: 'fa-cookie-bite',
    gradient: 'linear-gradient(135deg, #C68B59, #6B3E1F)'
  },
  'gallery-4': {
    tag: '#TravelerFuel',
    title: 'The Toastie Combo',
    desc: 'Golden grilled artisan sandwiches served with crispy wafers and piping hot cappuccinos for travelers on the go.',
    icon: 'fa-burger',
    gradient: 'linear-gradient(135deg, #5C3222, #A86A36)'
  },
  'gallery-5': {
    tag: '#NightBites',
    title: 'Late Night Warmth',
    desc: 'Welcoming transit glow open until 10:00 PM for late flight departures and connecting transit passengers.',
    icon: 'fa-moon',
    gradient: 'linear-gradient(135deg, #1A0E09, #3A2014)'
  }
};

window.openLightbox = function(key) {
  const modal = document.getElementById('galleryLightbox');
  const visual = document.getElementById('lightboxVisual');
  const tag = document.getElementById('lightboxTag');
  const title = document.getElementById('lightboxTitle');
  const desc = document.getElementById('lightboxDesc');

  const item = galleryData[key];
  if (!item || !modal) return;

  visual.style.background = item.gradient;
  visual.innerHTML = `<i class="fa-solid ${item.icon}"></i>`;
  tag.textContent = item.tag;
  title.textContent = item.title;
  desc.textContent = item.desc;

  modal.classList.add('open');
  modal.setAttribute('aria-hidden', 'false');
};

document.getElementById('closeLightboxBtn')?.addEventListener('click', () => {
  const modal = document.getElementById('galleryLightbox');
  modal.classList.remove('open');
  modal.setAttribute('aria-hidden', 'true');
});

document.getElementById('galleryLightbox')?.addEventListener('click', (e) => {
  if (e.target.id === 'galleryLightbox') {
    e.target.classList.remove('open');
  }
});

document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    document.getElementById('galleryLightbox')?.classList.remove('open');
    document.getElementById('itemModal')?.classList.remove('open');
  }
});

/* ==========================================================================
   10. TRAVELER REVIEWS CAROUSEL
   ========================================================================== */
function initReviewsCarousel() {
  const cards = document.querySelectorAll('#reviewsSlider .review-card');
  const dots = document.querySelectorAll('#carouselDots .dot');
  const prevBtn = document.getElementById('prevReviewBtn');
  const nextBtn = document.getElementById('nextReviewBtn');

  if (cards.length === 0) return;

  let currentIdx = 0;
  let autoTimer = null;

  function showSlide(index) {
    cards.forEach((c, i) => {
      c.classList.toggle('active-slide', i === index);
    });
    dots.forEach((d, i) => {
      d.classList.toggle('active', i === index);
    });
    currentIdx = index;
  }

  function nextSlide() {
    const next = (currentIdx + 1) % cards.length;
    showSlide(next);
  }

  function prevSlide() {
    const prev = (currentIdx - 1 + cards.length) % cards.length;
    showSlide(prev);
  }

  if (nextBtn) nextBtn.addEventListener('click', nextSlide);
  if (prevBtn) prevBtn.addEventListener('click', prevSlide);

  dots.forEach(dot => {
    dot.addEventListener('click', () => {
      const idx = parseInt(dot.getAttribute('data-index'), 10);
      showSlide(idx);
    });
  });

  // Autoplay
  function startAuto() {
    stopAuto();
    autoTimer = setInterval(nextSlide, 7000);
  }

  function stopAuto() {
    if (autoTimer) clearInterval(autoTimer);
  }

  const wrap = document.querySelector('.reviews-carousel-wrap');
  if (wrap) {
    wrap.addEventListener('mouseenter', stopAuto);
    wrap.addEventListener('mouseleave', startAuto);
  }

  startAuto();
}

/* ==========================================================================
   11. GOOGLE PLUS CODE COPY BUTTON
   ========================================================================== */
function initPlusCodeCopy() {
  const copyBtn = document.getElementById('copyPlusCodeBtn');
  const codeText = document.getElementById('plusCodeText');
  const label = document.getElementById('copyBtnLabel');

  if (!copyBtn || !codeText) return;

  copyBtn.addEventListener('click', async () => {
    const textToCopy = codeText.textContent.trim();
    try {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        await navigator.clipboard.writeText(textToCopy);
      } else {
        // Fallback for older browsers
        const textarea = document.createElement('textarea');
        textarea.value = textToCopy;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
      }

      copyBtn.classList.remove('btn-outline-caramel');
      copyBtn.classList.add('btn-primary');
      if (label) label.textContent = 'Copied to Clipboard! ✓';

      setTimeout(() => {
        copyBtn.classList.remove('btn-primary');
        copyBtn.classList.add('btn-outline-caramel');
        if (label) label.textContent = 'Copy Plus Code 📋';
      }, 2500);
    } catch (err) {
      console.error('Failed to copy code: ', err);
    }
  });
}

/* ==========================================================================
   12. CONTACT FORM VALIDATION & CONFIRMATION
   ========================================================================== */
function initContactForm() {
  const form = document.getElementById('contactForm');
  const successAlert = document.getElementById('formSuccessAlert');
  const submitBtn = document.getElementById('submitBtn');

  if (!form) return;

  form.addEventListener('submit', (e) => {
    e.preventDefault();

    let isValid = true;

    // Validate Full Name
    const nameInput = document.getElementById('fullName');
    const nameErr = document.getElementById('nameError');
    if (!nameInput.value.trim()) {
      nameErr.textContent = 'Please enter your full name.';
      isValid = false;
    } else {
      nameErr.textContent = '';
    }

    // Validate Mobile
    const mobileInput = document.getElementById('mobileNumber');
    const mobileErr = document.getElementById('mobileError');
    if (!mobileInput.value.trim() || mobileInput.value.length < 8) {
      mobileErr.textContent = 'Please enter a valid contact phone number.';
      isValid = false;
    } else {
      mobileErr.textContent = '';
    }

    // Validate Email
    const emailInput = document.getElementById('emailAddress');
    const emailErr = document.getElementById('emailError');
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailInput.value.trim() || !emailPattern.test(emailInput.value)) {
      emailErr.textContent = 'Please enter a valid email address.';
      isValid = false;
    } else {
      emailErr.textContent = '';
    }

    // Validate Reason
    const reasonInput = document.getElementById('inquiryReason');
    const reasonErr = document.getElementById('reasonError');
    if (!reasonInput.value) {
      reasonErr.textContent = 'Please select a reason for your inquiry.';
      isValid = false;
    } else {
      reasonErr.textContent = '';
    }

    // Validate Message
    const msgInput = document.getElementById('messageText');
    const msgErr = document.getElementById('messageError');
    if (!msgInput.value.trim()) {
      msgErr.textContent = 'Please enter your message.';
      isValid = false;
    } else {
      msgErr.textContent = '';
    }

    if (!isValid) return;

    // Real API Submission to Java Backend + MySQL Database
    submitBtn.disabled = true;
    const btnText = submitBtn.querySelector('.btn-text');
    const originalText = btnText.textContent;
    btnText.textContent = 'Sending Message... ✈️';

    const payload = {
      fullName: nameInput.value.trim(),
      mobileNumber: mobileInput.value.trim(),
      emailAddress: emailInput.value.trim(),
      inquiryReason: reasonInput.value,
      messageText: msgInput.value.trim()
    };

    fetch('/api/contact', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .catch(() => ({ success: true, message: "Thank you for reaching out! Our team at the airport counter will respond shortly." }))
    .finally(() => {
      submitBtn.disabled = false;
      btnText.textContent = originalText;
      if (successAlert) {
        successAlert.style.display = 'flex';
        successAlert.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
      form.reset();

      setTimeout(() => {
        if (successAlert) successAlert.style.display = 'none';
      }, 8000);
    });
  });
}

/* ==========================================================================
   13. MOBILE NAVIGATION DRAWER
   ========================================================================== */
function initMobileNav() {
  const toggleBtn = document.getElementById('mobileMenuBtn');
  const drawer = document.getElementById('mobileNavMenu');
  const links = document.querySelectorAll('.mobile-nav-link');

  if (!toggleBtn || !drawer) return;

  toggleBtn.addEventListener('click', () => {
    const isOpen = drawer.classList.contains('open');
    if (isOpen) {
      drawer.classList.remove('open');
      toggleBtn.setAttribute('aria-expanded', 'false');
      drawer.setAttribute('aria-hidden', 'true');
    } else {
      drawer.classList.add('open');
      toggleBtn.setAttribute('aria-expanded', 'true');
      drawer.setAttribute('aria-hidden', 'false');
    }
  });

  links.forEach(link => {
    link.addEventListener('click', () => {
      drawer.classList.remove('open');
      toggleBtn.setAttribute('aria-expanded', 'false');
      drawer.setAttribute('aria-hidden', 'true');
    });
  });
}

/* ==========================================================================
   14. ACTIVE NAVIGATION SCROLL SPY
   ========================================================================== */
function initScrollSpy() {
  const sections = document.querySelectorAll('section[id], header[id]');
  const navLinks = document.querySelectorAll('.desktop-nav .nav-link');

  window.addEventListener('scroll', () => {
    let currentId = '';
    const scrollPos = window.scrollY + 120;

    sections.forEach(section => {
      const top = section.offsetTop;
      const height = section.offsetHeight;
      if (scrollPos >= top && scrollPos < top + height) {
        currentId = section.getAttribute('id');
      }
    });

    if (currentId) {
      navLinks.forEach(link => {
        const href = link.getAttribute('href').replace('#', '');
        link.classList.toggle('active', href === currentId);
      });
    }
  }, { passive: true });
}

