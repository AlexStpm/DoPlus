describe('End to end tests', () => {
  it('example', () => {
    cy.visit('http://localhost:3000/')
    cy.get('input[id="username"]').type("rootadmin")
    cy.get('input[id="password"]').type("123")
    cy.contains('Log In').click()
    cy.get('h1').contains("Manage users")
  })
})