function searchService() {
    const query = document.getElementById("service-search").value;
    document.getElementById("service-results").innerHTML = `<p>Searching for "${query}"...</p>`;
}

function bookService() {
    const bookingTime = document.getElementById("booking-time").value;
    alert("You have successfully booked the service for " + bookingTime);
}
