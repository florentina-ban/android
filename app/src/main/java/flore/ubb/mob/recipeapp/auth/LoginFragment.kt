package flore.ubb.mob.recipeapp.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import flore.ubb.mob.recipeapp.R
import flore.ubb.mob.recipeapp.auth.data.TokenHolder
import kotlinx.android.synthetic.main.fragment_login.*
import flore.ubb.mob.recipeapp.core.Result;

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        setupLoginForm()
    }

    private fun setupLoginForm() {
        viewModel.loginFormState.observe(viewLifecycleOwner, { loginState ->
            login_button.isEnabled = loginState.isDataValid
            if (loginState.usernameError != null) {
                username_tb.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password_tb.error = getString(loginState.passwordError)
            }
        })
        viewModel.loginResult.observe(viewLifecycleOwner, { loginResult ->
            loading.visibility = View.GONE
            if (loginResult is Result.Success<*>) {
                findNavController().navigate(R.id.action_login_to_rcipeList)
            } else if (loginResult is Result.Error) {
                login_error.text = "Login error ${loginResult.exception.message}"
                login_error.visibility = View.VISIBLE
            }
        })
        username_tb.afterTextChanged {
            viewModel.loginDataChanged(
                username_tb.text.toString(),
                password_tb.text.toString()
            )
        }
        password_tb.afterTextChanged {
            viewModel.loginDataChanged(
                username_tb.text.toString(),
                password_tb.text.toString()
            )
        }
        login_button.setOnClickListener {
            loading.visibility = View.VISIBLE
            login_error.visibility = View.GONE
            viewModel.login(username_tb.text.toString(), password_tb.text.toString())
        }

        register_button.setOnClickListener {
            loading.visibility = View.VISIBLE
            login_error.visibility = View.GONE
            viewModel.register(username_tb.text.toString(), password_tb.text.toString())
        }
    }
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
